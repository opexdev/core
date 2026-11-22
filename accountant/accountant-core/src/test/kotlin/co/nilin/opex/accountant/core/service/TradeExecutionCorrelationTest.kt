package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class TradeExecutionCorrelationTest {
    @Test
    fun `normal processing and retry preserve operation and individual action references`() = runBlocking<Unit> {
        val loader = mockk<FinancialActionLoader>()
        val persister = mockk<FinancialActionPersister>(relaxed = true)
        val wallet = mockk<WalletProxy>(relaxed = true)
        val actions = listOf(FinancialActionCategory.TRADE, FinancialActionCategory.TRADE,
            FinancialActionCategory.FEE, FinancialActionCategory.FEE).mapIndexed { index, category ->
            FinancialAction(null, "trade", "order-$index", "ETH", BigDecimal.ONE,
                "payer-$index", WalletType.EXCHANGE, "recipient-$index", WalletType.MAIN,
                LocalDateTime.now(), category, id = index.toLong() + 1,
                operationId = "accountant-trade:execution-1")
        }
        coEvery { loader.loadReadyToProcess(0, 10) } returns actions
        coEvery { loader.loadRetries(10) } returns actions
        val manager = FinancialActionJobManagerImpl(loader, persister, wallet)
        manager.processFinancialActions(0, 10)
        manager.retryFinancialActions(10)
        actions.forEach { action ->
            coVerify(exactly = 2) {
                wallet.transfer(action.symbol, action.senderWalletType, action.sender,
                    action.receiverWalletType, action.receiver, action.amount,
                    action.eventType + action.pointer, "accountant:fiActions:${action.uuid}",
                    action.category.toString(), "accountant-trade:execution-1")
                persister.updateStatusNewTx(action, FinancialActionStatus.PROCESSED)
            }
            coVerify(exactly = 1) { persister.retrySuccessful(action) }
        }
    }
}
