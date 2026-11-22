package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.WithdrawType
import co.nilin.opex.wallet.core.service.WithdrawService
import co.nilin.opex.wallet.core.spi.*
import co.nilin.opex.wallet.ports.kafka.listener.model.DepositEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.WithdrawRequestEvent
import co.nilin.opex.wallet.ports.kafka.listener.submitter.DepositSubmitter
import co.nilin.opex.wallet.ports.kafka.listener.submitter.WithdrawRequestSubmitter
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture
import java.math.BigDecimal
import java.time.LocalDateTime

class FundingPublicationTest {
    private class Fixture {
        val owners = mockk<WalletOwnerManager>()
        val wallets = mockk<WalletManager>()
        val currencies = mockk<CurrencyServiceManager>()
        val gateway = mockk<CurrencyServiceV2>()
        val transfers = mockk<TransferManager>()
        val deposits = mockk<DepositPersister>()
        val withdrawals = mockk<WithdrawPersister>()
        val depositEvents = mutableListOf<DepositEvent>()
        val withdrawEvents = mutableListOf<WithdrawRequestEvent>()
        val commands = mutableListOf<TransferCommand>()
        val currency = CurrencyCommand("USDT", precision = BigDecimal("0.000001"))
        val customer = WalletOwner(1, "customer", "Customer", "registered", true, true, true)
        val system = customer.copy(id = 2, uuid = "system")
        val admin = customer.copy(id = 3, uuid = "admin")
        val depositKafka = mockk<KafkaTemplate<String, DepositEvent>>()
        val withdrawKafka = mockk<KafkaTemplate<String?, WithdrawRequestEvent>>()
        val transferService = TransferService(transfers, currencies, wallets, owners, mockk(), mockk(), mockk(), mockk())
        val withdrawService: WithdrawService
        val depositService: DepositService

        init {
            every { owners.systemUuid } returns "system"
            coEvery { owners.findWalletOwner(any()) } answers {
                when (firstArg<String>()) { "customer" -> customer; "admin" -> admin; else -> system }
            }
            coEvery { currencies.fetchCurrency(any()) } returns currency
            coEvery { wallets.findWalletByOwnerAndCurrencyAndType(any(), any(), any()) } answers {
                val owner = firstArg<WalletOwner>()
                Wallet(owner.id!! * 10, owner, Amount(currency, BigDecimal("10000")), currency, secondArg(), 0)
            }
            coEvery { wallets.findWalletById(any()) } returns Wallet(11, customer,
                Amount(currency, BigDecimal("10000")), currency, WalletType.CASHOUT, 0)
            coEvery { transfers.transfer(capture(commands)) } answers {
                val c = firstArg<TransferCommand>()
                TransferResultDetailed(TransferResult(1, c.sourceWallet.owner.uuid, c.sourceWallet.type,
                    c.sourceWallet.balance, c.sourceWallet.balance, c.amount, c.destWallet.owner.uuid,
                    c.destWallet.type, c.destAmount, c.sourceWallet.id, c.destWallet.id), "wallet-tx")
            }
            coEvery { deposits.persist(any()) } answers { firstArg<Deposit>().copy(id = 41) }
            coEvery { deposits.existsByTransactionRef(any()) } returns false
            coEvery { withdrawals.persist(any()) } answers { firstArg<Withdraw>().copy(withdrawId = 42) }
            every { depositKafka.send(any<String>(), any<String>(), capture(depositEvents)) } answers {
                SettableListenableFuture<SendResult<String, DepositEvent>>().apply { set(mockk()) }
            }
            every { withdrawKafka.send(any<String>(), any<String>(), capture(withdrawEvents)) } answers {
                SettableListenableFuture<SendResult<String?, WithdrawRequestEvent>>().apply { set(mockk()) }
            }
            withdrawService = WithdrawService(withdrawals, wallets, owners, currencies, transfers, mockk(relaxed = true),
                mockk(), mockk(), mockk(), WithdrawRequestSubmitter(withdrawKafka), null, mockk(), mockk(), mockk(),
                mockk(), "system", false, 0, false)
            depositService = DepositService(owners, wallets, deposits, transferService, TraceDepositService(deposits),
                gateway, DepositSubmitter(depositKafka), transfers, currencies, mockk(), true)
        }
    }

    @Test
    fun `reward-funded deposits remain deposits and preserve funding reason and movement linkage`() = runBlocking<Unit> {
        val f = Fixture()
        f.depositService.commitPaymentDeposit(PaymentDepositRequest("customer", BigDecimal.TEN, "USDT",
            "reward-claim-1", "Reward payout", TransferMethod.REWARD))
        val command = f.commands.single()
        val event = f.depositEvents.single()
        assertThat(command.transferCategory).isEqualTo(TransferCategory.DEPOSIT)
        assertThat(command.transferMethod).isEqualTo(TransferMethod.REWARD)
        assertThat(command.operationId).isEqualTo(event.depositUuid)
        assertThat(event.depositId).isEqualTo(41)
        assertThat(event.transferMethod).isEqualTo("REWARD")
        assertThat(event.senderPartyId).isEqualTo("system")
        assertThat(event.receiverPartyId).isEqualTo("customer")
        assertThat(event.walletTransactionRef).isEqualTo("wallet-tx")
        assertThat(event.businessReference).isEqualTo("reward-claim-1")
    }

    @Test
    fun `manual deposits preserve acting admin gateway attachment and actual wallets`() = runBlocking<Unit> {
        val f = Fixture()
        coEvery { f.gateway.fetchCurrencyGateway("gateway", "USDT") } returns OffChainGatewayCommand(
            TransferMethod.MANUALLY, currencySymbol = "USDT", gatewayUuid = "gateway")
        f.depositService.depositManually("USDT", "customer", "admin", BigDecimal.TEN,
            ManualTransferRequest("manual-deposit", "Adjustment", "receipt", "gateway"))
        val event = f.depositEvents.single()
        assertThat(event.transferCategory).isEqualTo("DEPOSIT_MANUALLY")
        assertThat(event.transferMethod).isEqualTo("MANUALLY")
        assertThat(event.applicator).isEqualTo("admin")
        assertThat(event.senderPartyId).isEqualTo("admin")
        assertThat(event.gatewayId).isEqualTo("gateway")
        assertThat(event.attachment).isEqualTo("receipt")
        assertThat(f.commands.single().applicator).isEqualTo("admin")
    }

    @Test
    fun `normal chain deposit preserves channel network and transaction reference`() = runBlocking<Unit> {
        val f = Fixture()
        f.depositService.deposit("USDT", "customer", WalletType.MAIN, null, BigDecimal.TEN, "Chain credit",
            "chain-tx", "TRON", null, DepositType.ON_CHAIN, null, null, null, sourceAddress = "chain-source-address")
        val event = f.depositEvents.single()
        assertThat(event.operationType).isEqualTo("ON_CHAIN")
        assertThat(event.network).isEqualTo("TRON")
        assertThat(event.depositRef).isEqualTo("chain-tx")
        assertThat(event.sourceAddress).isEqualTo("chain-source-address")
        assertThat(event.sourceSymbol).isEqualTo("USDT")
        assertThat(event.senderWalletId).isEqualTo("20")
        assertThat(event.receiverWalletId).isEqualTo("10")
    }

    @Test
    fun `manual withdrawal publishes completed lifecycle with real amount and admin`() = runBlocking<Unit> {
        val f = Fixture()
        coEvery { f.gateway.fetchCurrencyGateway("gateway", "USDT") } returns OffChainGatewayCommand(
            TransferMethod.MANUALLY, currencySymbol = "USDT", gatewayUuid = "gateway", withdrawMax = BigDecimal("1000"))
        ManualWithdrawService(f.transferService, f.withdrawals, f.gateway, f.withdrawService)
            .withdrawManually("USDT", "admin", "customer", BigDecimal.TEN,
                ManualTransferRequest("manual-withdraw", "Correction", "receipt", "gateway"))
        val event = f.withdrawEvents.single()
        assertThat(event.status).isEqualTo(WithdrawStatus.DONE)
        assertThat(event.withdrawId).isEqualTo(42)
        assertThat(event.withdrawUuid).isEqualTo(f.commands.single().operationId)
        assertThat(event.transferCategory).isEqualTo("WITHDRAW_MANUALLY")
        assertThat(event.transferMethod).isEqualTo("MANUALLY")
        assertThat(event.destinationAmount).isEqualByComparingTo("10")
        assertThat(event.appliedCustomerFee).isEqualByComparingTo("0")
        assertThat(event.applicator).isEqualTo("admin")
        assertThat(event.requestTransactionId).isNull()
        assertThat(event.finalizedTransactionId).isEqualTo("wallet-tx")
        assertThat(event.counterpartyReferenceId).isEqualTo("manual-withdraw")
        assertThat(event.completionDate).isNotNull()
    }

    @Test
    fun `normal completed withdrawal retains hold payout and fee assessment references`() = runBlocking<Unit> {
        val f = Fixture()
        coEvery { f.withdrawals.findByWithdrawUuid("withdraw-1") } returns Withdraw(withdrawId = 42,
            withdrawUuid = "withdraw-1", ownerUuid = "customer", currency = "USDT", wallet = 11,
            amount = BigDecimal("100"), appliedFee = BigDecimal.ONE, requestTransaction = "hold-tx",
            status = WithdrawStatus.ACCEPTED, withdrawType = WithdrawType.OFF_CHAIN,
            transferMethod = TransferMethod.SHEBA, gatewayUuid = "gateway", destAddress = "account", destSymbol = "USDT")
        f.withdrawService.doneWithdraw(WithdrawDoneCommand("withdraw-1", BigDecimal("100"), "payout-ref", "Paid", "receipt", "admin"))
        val event = f.withdrawEvents.single()
        assertThat(event.amount).isEqualByComparingTo("101")
        assertThat(event.principalAmount).isEqualByComparingTo("100")
        assertThat(event.appliedCustomerFee).isEqualByComparingTo("1")
        assertThat(event.transferMethod).isEqualTo("SHEBA")
        assertThat(event.gatewayId).isEqualTo("gateway")
        assertThat(event.requestTransactionId).isEqualTo("hold-tx")
        assertThat(event.finalizedTransactionId).isEqualTo("wallet-tx")
        assertThat(event.applicator).isEqualTo("admin")
        assertThat(event.counterpartyReferenceId).isEqualTo("payout-ref")
        assertThat(f.commands.single().operationId).isEqualTo("withdraw-1")
    }
}
