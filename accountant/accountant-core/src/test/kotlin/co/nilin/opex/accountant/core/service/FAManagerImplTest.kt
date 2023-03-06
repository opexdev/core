package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.FinancialActionPublisher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat

class FAManagerImplTest {

    private val financialActionLoader = mockk<FinancialActionLoader>()
    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val financialActionPublisher = mockk<FinancialActionPublisher>()

    private val sut = FinancialActionProcessorImpl(
        financialActionLoader,
        financialActionPersister,
        financialActionPublisher
    )

    init {
        coEvery { financialActionLoader.loadUnprocessed(any(), any()) } returns arrayListOf(fa2, fa1, fa5, fa3, fa4)
        coEvery { financialActionPersister.persist(any()) } returns arrayListOf()
        coEvery { financialActionPersister.persistWithStatus(any(), any()) } returns Unit
        coEvery { financialActionPublisher.publish(any()) } returns Unit
    }

    @Test
    fun givenFALoaded_validateParentsAreFirstInLine(): Unit = runBlocking {
        val list = arrayListOf<FinancialAction>()
        sut.extractParents(fa5, list)

        assertThat(list.size).isEqualTo(4)
        assertThat(list[0].id).isEqualTo(1)
        assertThat(list[1].id).isEqualTo(2)
        assertThat(list[2].id).isEqualTo(3)
        assertThat(list[3].id).isEqualTo(5)
    }

    companion object {
        private val fa1 = FinancialAction(
            null,
            "",
            "",
            "BTCUSDT",
            1.0.toBigDecimal(),
            "w1",
            "main",
            "w2",
            "exchange",
            LocalDateTime.now(),
            id = 1
        )
        private val fa2 = FinancialAction(
            fa1,
            "",
            "",
            "BTCUSDT",
            2.0.toBigDecimal(),
            "w2",
            "exchange",
            "w1",
            "main",
            LocalDateTime.now(),
            id = 2
        )
        private val fa3 = FinancialAction(
            fa2,
            "",
            "",
            "BTCUSDT",
            1.5.toBigDecimal(),
            "w2",
            "main",
            "w2",
            "exchange",
            LocalDateTime.now(),
            id = 3
        )
        private val fa4 = FinancialAction(
            fa2,
            "",
            "",
            "BTCUSDT",
            1.0.toBigDecimal(),
            "w1",
            "main",
            "w1",
            "exchange",
            LocalDateTime.now(),
            id = 4
        )
        private val fa5 = FinancialAction(
            fa3,
            "",
            "",
            "BTCUSDT",
            1.1.toBigDecimal(),
            "w1",
            "main",
            "w3",
            "exchange",
            LocalDateTime.now(),
            id = 5
        )
    }

}