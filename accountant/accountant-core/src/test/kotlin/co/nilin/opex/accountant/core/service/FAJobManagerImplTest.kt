package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class FAJobManagerImplTest {

    private val financialActionLoader = mockk<FinancialActionLoader>()
    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val walletProxy = mockk<WalletProxy>()

    private val sut = FinancialActionJobManagerImpl(financialActionLoader, financialActionPersister, walletProxy)

    init {
        coEvery { financialActionLoader.loadUnprocessed(any(), any()) } returns listOf(DOC.fa, DOC.fa)
        coEvery { financialActionPersister.updateStatus(any(), any()) } returns Unit
    }

    @Test
    fun given2FALoaded_whenProcessing_thenVerifyThatTransferProxyCalled2Times() = runBlocking {
        coEvery { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) } returns Unit
        sut.processFinancialActions(0, 2)
        coVerify(exactly = 2) { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 2) { financialActionPersister.updateStatus(any(), any()) }
    }

    @Test
    fun given2FALoaded_whenProcessingFailed_thenUpdateStatusCalledRegardless() = runBlocking {
        coEvery {
            walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException()

        sut.processFinancialActions(0, 2)
        coVerify(exactly = 2) { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 2) { financialActionPersister.updateStatus(any(), any()) }
    }

}