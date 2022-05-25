package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FAJobManagerImplTest {

    private val financialActionLoader = mockk<FinancialActionLoader>()
    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val walletProxy = mockk<WalletProxy>()

    private val sut = FinancialActionJobManagerImpl(financialActionLoader, financialActionPersister, walletProxy)

    private fun stub() {
        val fa = FinancialAction(
            null,
            TradeEvent::class.java.name,
            "trade_id",
            "BTC_USDT",
            10000.0.toBigDecimal(),
            "user_parent",
            "main",
            "system",
            "main",
            LocalDateTime.now()
        )

        coEvery { financialActionLoader.loadUnprocessed(any(), any()) } returns listOf(fa, fa)
        coEvery { financialActionPersister.updateStatus(any(), any()) } returns Unit
    }

    @Test
    fun given2FALoaded_whenProcessing_thenVerifyThatTransferProxyCalled2Times() = runBlocking {
        stub()
        coEvery { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) } returns Unit
        sut.processFinancialActions(0, 2)
        coVerify(exactly = 2) { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 2) { financialActionPersister.updateStatus(any(), any()) }
    }

    @Test
    fun given2FALoaded_whenProcessingFailed_thenUpdateStatusCalledRegardless() = runBlocking {
        stub()
        coEvery {
            walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException()

        sut.processFinancialActions(0, 2)
        coVerify(exactly = 2) { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 2) { financialActionPersister.updateStatus(any(), any()) }
    }

}