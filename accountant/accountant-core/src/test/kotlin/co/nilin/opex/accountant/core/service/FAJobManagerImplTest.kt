package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat

class FAJobManagerImplTest {

    private val financialActionLoader = mockk<FinancialActionLoader>()
    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val walletProxy = mockk<WalletProxy>()

    private val sut = FinancialActionJobManagerImpl(financialActionLoader, financialActionPersister, walletProxy)

    init {
        coEvery { financialActionLoader.loadUnprocessed(any(), any()) } returns listOf(Valid.fa, Valid.fa)
        coEvery { financialActionPersister.updateBatchStatus(any(), any()) } returns Unit
    }

    @Test
    fun given2FALoaded_whenProcessingFailed_thenUpdateStatusCalledRegardless() = runBlocking {
        coEvery {
            walletProxy.batchTransfer(any())
        } throws IllegalStateException()

        sut.processFinancialActions(0, 2)
        coVerify(exactly = 1) {
            walletProxy.batchTransfer(any())
        }
    }

    @Test
    fun givenFALoaded_validateParentsAreFirstInLine(): Unit = runBlocking {
        val fa1 = FinancialAction(null, "", "", "", BigDecimal.ZERO, "", "", "", "", LocalDateTime.now(), id = 1)
        val fa2 = FinancialAction(fa1, "", "", "", BigDecimal.ZERO, "", "", "", "", LocalDateTime.now(), id = 2)
        val fa3 = FinancialAction(fa1, "", "", "", BigDecimal.ZERO, "", "", "", "", LocalDateTime.now(), id = 3)
        val fa4 = FinancialAction(fa3, "", "", "", BigDecimal.ZERO, "", "", "", "", LocalDateTime.now(), id = 4)
        val fa5 = FinancialAction(null, "", "", "", BigDecimal.ZERO, "", "", "", "", LocalDateTime.now(), id = 5)
        val list = arrayListOf(fa5, fa4, fa3, fa2, fa1)

        val flatten = sut.sortAndFlattenFA(list)

        assertThat(flatten.indexOf(fa1)).isLessThan(flatten.indexOf(fa2))
        assertThat(flatten.indexOf(fa1)).isLessThan(flatten.indexOf(fa3))
        assertThat(flatten.indexOf(fa1)).isLessThan(flatten.indexOf(fa4))
        assertThat(flatten.indexOf(fa3)).isLessThan(flatten.indexOf(fa4))
    }

}