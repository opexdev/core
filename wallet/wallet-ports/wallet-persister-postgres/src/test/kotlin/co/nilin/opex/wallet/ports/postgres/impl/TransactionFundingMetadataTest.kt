package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.WalletManager
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.model.TransactionModel
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class TransactionFundingMetadataTest {
    @Test
    fun `funding reason and actor survive transaction persistence and retry loading`() = runBlocking<Unit> {
        val repo = mockk<TransactionRepository>()
        val wallets = mockk<WalletManager>()
        val captured = slot<TransactionModel>()
        every { repo.save(capture(captured)) } answers { Mono.just(captured.captured.copy(id = 1)) }
        val manager = TransactionManagerImpl(repo, mockk(), wallets, mockk())
        val currency = CurrencyCommand("USDT", precision = BigDecimal("0.000001"))
        val owner = WalletOwner(1, "system", "System", "registered", true, true, true)
        val source = Wallet(1, owner, Amount(currency, BigDecimal.TEN), currency, WalletType.MAIN, 0)
        val dest = source.copy(id = 2, owner = owner.copy(id = 2, uuid = "customer"))
        val time = LocalDateTime.parse("2026-11-18T10:00:00")
        manager.save(Transaction(source, dest, BigDecimal.ONE, BigDecimal.ONE, "reward", "claim-1",
            TransferCategory.DEPOSIT, time, "deposit-1", TransferMethod.REWARD, "admin"))
        every { repo.findByTransferRef("claim-1") } returns Mono.just(captured.captured.copy(id = 1))
        coEvery { wallets.findWalletById(1) } returns source
        coEvery { wallets.findWalletById(2) } returns dest
        val loaded = manager.findTransactionByTransferRef("claim-1")!!.transaction
        assertThat(loaded.operationId).isEqualTo("deposit-1")
        assertThat(loaded.transferMethod).isEqualTo(TransferMethod.REWARD)
        assertThat(loaded.applicator).isEqualTo("admin")
        assertThat(loaded.transactionDate).isEqualTo(time)
        assertThat(loaded.transferCategory).isEqualTo(TransferCategory.DEPOSIT)
    }

    @Test
    fun `deposit gateway and funding method survive the database model mapping`() {
        val deposit = Deposit("customer", "deposit-1", "USDT", BigDecimal.TEN, status = DepositStatus.DONE,
            depositType = DepositType.OFF_CHAIN, attachment = "receipt", transferMethod = TransferMethod.MANUALLY,
            applicator = "admin", gatewayUuid = "gateway-1", id = 1)
        val reloaded = deposit.toModel().toDto()
        assertThat(reloaded.gatewayUuid).isEqualTo("gateway-1")
        assertThat(reloaded.transferMethod).isEqualTo(TransferMethod.MANUALLY)
        assertThat(reloaded.applicator).isEqualTo("admin")
        assertThat(reloaded.id).isEqualTo(1)
    }
}
