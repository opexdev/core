package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialActionStatus
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
        coEvery { financialActionLoader.loadUnprocessed(any(), any()) } returns listOf(Valid.fa, Valid.fa)
        coEvery { financialActionPersister.updateStatus(any(), any()) } returns Unit
    }

    @Test
    fun given2FALoaded_whenProcessing_thenVerifyThatTransferProxyCalled2Times() = runBlocking {
        coEvery { walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any()) } returns Unit
        sut.processFinancialActions(0, 2)
        with(Valid.fa) {
            coVerify(exactly = 2) {
                walletProxy.transfer(
                    eq(symbol),
                    eq(senderWalletType),
                    eq(sender),
                    eq(receiverWalletType),
                    eq(receiver),
                    eq(amount),
                    eq(eventType + pointer),
                    any()
                )
            }
            coVerify(exactly = 2) {
                financialActionPersister.updateStatus(
                    eq(this@with),
                    eq(FinancialActionStatus.PROCESSED)
                )
            }
        }
    }

    @Test
    fun given2FALoaded_whenProcessingFailed_thenUpdateStatusCalledRegardless() = runBlocking {
        coEvery {
            walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException()

        sut.processFinancialActions(0, 2)
        with(Valid.fa) {
            coVerify(exactly = 2) {
                walletProxy.transfer(
                    eq(symbol),
                    eq(senderWalletType),
                    eq(sender),
                    eq(receiverWalletType),
                    eq(receiver),
                    eq(amount),
                    eq(eventType + pointer),
                    any()
                )
            }
            coVerify(exactly = 2) {
                financialActionPersister.updateStatus(
                    eq(this@with),
                    eq(FinancialActionStatus.CREATED)
                )
            }
        }
    }

    @Test
    fun given2FALoaded_whenProcessingFailedAndRetryCountExceeded_thenUpdateStatusCalledRegardless() = runBlocking {
        coEvery {
            walletProxy.transfer(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException()

        coEvery { financialActionLoader.loadUnprocessed(any(), any()) } returns listOf(Valid.faHighRetry)

        sut.processFinancialActions(0, 1)
        with(Valid.faHighRetry) {
            coVerify(exactly = 1) {
                walletProxy.transfer(
                    eq(symbol),
                    eq(senderWalletType),
                    eq(sender),
                    eq(receiverWalletType),
                    eq(receiver),
                    eq(amount),
                    eq(eventType + pointer),
                    any()
                )
            }
            coVerify(exactly = 1) {
                financialActionPersister.updateStatus(
                    eq(this@with),
                    eq(FinancialActionStatus.ERROR)
                )
            }
        }
    }

}