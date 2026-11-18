package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SwapPublicationTest {
    @Test
    fun `publishes one complete conversion after both settlement legs`() = runBlocking<Unit> {
        val transfers = mockk<TransferManager>()
        val currencies = mockk<CurrencyServiceManager>()
        val wallets = mockk<WalletManager>()
        val owners = mockk<WalletOwnerManager>()
        val reservations = mockk<ReservedTransferManager>(relaxed = true)
        val publisher = mockk<SwapEventSubmitter>(relaxed = true)
        val customer = WalletOwner(1L, "customer", "Customer", "registered", true, true, true)
        val system = customer.copy(id = 2L, uuid = "system")
        val usdt = CurrencyCommand("USDT", precision = BigDecimal("0.000001"))
        val irt = CurrencyCommand("IRT", precision = BigDecimal.ONE)
        val reserve = ReservedTransfer(reserveNumber = "r-1", sourceSymbol = "USDT", destSymbol = "IRT",
            senderWalletType = WalletType.MAIN, senderUuid = customer.uuid,
            receiverWalletType = WalletType.MAIN, receiverUuid = customer.uuid,
            sourceAmount = BigDecimal("10"), reservedDestAmount = BigDecimal("1500000"))
        coEvery { reservations.fetchValidReserve("r-1") } returns reserve
        every { owners.systemUuid } returns "system"
        coEvery { owners.findWalletOwner("customer") } returns customer
        coEvery { owners.findWalletOwner("system") } returns system
        coEvery { currencies.fetchCurrency(any()) } answers {
            if (firstArg<FetchCurrency>().symbol == "USDT") usdt else irt
        }
        coEvery { wallets.findWalletByOwnerAndCurrencyAndType(any(), any(), any()) } answers {
            val owner = firstArg<WalletOwner>()
            val currency = thirdArg<CurrencyCommand>()
            Wallet(owner.id!! * 10 + if (currency.symbol == "USDT") 1 else 2, owner,
                Amount(currency, BigDecimal("9999999")), currency, WalletType.MAIN, 0L)
        }
        val commands = mutableListOf<TransferCommand>()
        coEvery { transfers.transfer(capture(commands)) } answers {
            val command = firstArg<TransferCommand>()
            TransferResultDetailed(TransferResult(1L, command.sourceWallet.owner.uuid,
                command.sourceWallet.type, command.sourceWallet.balance, command.sourceWallet.balance,
                command.amount, command.destWallet.owner.uuid, command.destWallet.type, command.destAmount,
                command.sourceWallet.id, command.destWallet.id), if (commands.size == 1) "101" else "102")
        }
        val service = TransferService(transfers, currencies, wallets, owners,
            mockk(relaxed = true), reservations, mockk(relaxed = true), publisher)
        val result = service.advanceTransfer("r-1", null, "business-ref", customer.uuid)
        assertThat(commands).hasSize(2)
        assertThat(commands.map { it.operationId }).containsOnly("swap:r-1")
        assertThat(commands.map { it.perspectivePartyId }).containsOnly("customer")
        assertThat(commands.map { it.amount.currency.symbol }).containsExactly("USDT", "IRT")
        assertThat(result.tx).isEqualTo("swap:r-1")
        coVerify(exactly = 1) { publisher.send(
            uuid = "customer", txId = "swap:r-1", sourceAsset = "USDT", sourceAmount = BigDecimal("10"),
            destAsset = "IRT", destAmount = BigDecimal("1500000"), createDate = any(),
            sourceTransactionId = "101", destinationTransactionId = "102",
            sourceReference = "business-ref-r-1-withdraw", destinationReference = "business-ref-r-1-deposit",
            systemPartyId = "system", receiverPartyId = "customer",
            sourceSenderWalletId = "11", sourceReceiverWalletId = "21",
            destinationSenderWalletId = "22", destinationReceiverWalletId = "12",
            sourceSenderWalletType = "MAIN", sourceReceiverWalletType = "MAIN",
            destinationSenderWalletType = "MAIN", destinationReceiverWalletType = "MAIN"
        ) }
    }
}
