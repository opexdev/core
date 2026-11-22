package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTransactionCorrelationTest {
    @Test
    fun `trade rows and fee row retain common operation and individual movement references`() = runBlocking<Unit> {
        val wallets = mockk<WalletManager>(relaxed = true)
        val owners = mockk<WalletOwnerManager>(relaxed = true)
        val transactions = mockk<TransactionManager>(relaxed = true)
        val history = mockk<UserTransactionManager>(relaxed = true)
        val rows = mutableListOf<UserTransaction>()
        val transfers = mutableListOf<Transaction>()
        coEvery { owners.isWithdrawAllowed(any(), any()) } returns true
        coEvery { owners.isDepositAllowed(any(), any()) } returns true
        coEvery { wallets.isWithdrawAllowed(any(), any()) } returns true
        coEvery { wallets.isDepositAllowed(any(), any()) } returns true
        coEvery { wallets.findWalletById(any()) } returns VALID.SOURCE_WALLET
        coEvery { wallets.findWallet(any(), any(), any()) } returns mockk(relaxed = true)
        coEvery { transactions.findTransactionByTransferRef(any()) } returns null
        coEvery { transactions.save(capture(transfers)) } answers { transfers.size.toLong() }
        coEvery { history.save(capture(rows)) } returns Unit
        val manager = TransferManagerImpl(wallets, mockk(relaxed = true), owners, transactions,
            history, mockk(relaxed = true))
        listOf(TransferCategory.TRADE, TransferCategory.FEE).forEach { category ->
            manager.transfer(VALID.TRANSFER_COMMAND.copy(transferCategory = category,
                transferRef = "accountant:fiActions:${category.name}-action",
                operationId = "accountant-trade:execution-1"))
        }
        assertThat(rows).hasSize(3)
        assertThat(rows.map { it.uuid }.distinct()).hasSize(3)
        assertThat(rows.map { it.operationId }).containsOnly("accountant-trade:execution-1")
        assertThat(rows.take(2).map { it.financialActionId }).containsOnly("TRADE-action")
        assertThat(rows.last().financialActionId).isEqualTo("FEE-action")
        rows.forEach { row ->
            assertThat(row.date).isEqualTo(transfers[(row.txId - 1).toInt()].transactionDate)
        }
    }
}
