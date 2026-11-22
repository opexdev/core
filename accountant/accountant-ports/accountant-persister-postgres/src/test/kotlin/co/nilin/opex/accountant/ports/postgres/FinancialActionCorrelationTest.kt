package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.ports.postgres.dao.*
import co.nilin.opex.accountant.ports.postgres.impl.*
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class FinancialActionCorrelationTest {
    @Test
    fun `operation reference survives persistence and every loading path`() = runBlocking<Unit> {
        val repository = mockk<FinancialActionRepository>()
        val retries = mockk<FinancialActionRetryRepository>()
        val errors = mockk<FinancialActionErrorRepository>()
        val persister = FinancialActionPersisterImpl(repository, retries, errors)
        val loader = FinancialActionLoaderImpl(repository, retries, errors)
        val action = FinancialAction(null, "trade", "order-1", "ETH", BigDecimal.ONE,
            "payer", WalletType.EXCHANGE, "recipient", WalletType.MAIN, LocalDateTime.now(),
            FinancialActionCategory.FEE, operationId = "accountant-trade:execution-1")
        var saved: FinancialActionModel? = null
        every { repository.saveAll(any<Iterable<FinancialActionModel>>()) } answers {
            saved = firstArg<Iterable<FinancialActionModel>>().single().copy(id = 1L)
            Flux.just(saved!!)
        }
        persister.persist(listOf(action))
        assertEquals(action.operationId, saved!!.operationId)
        every { repository.findById(1L) } answers { Mono.just(saved!!) }
        every { repository.findReadyToProcess(any()) } answers { flowOf(saved!!) }
        every { retries.findAllRetries(any(), any()) } answers { Flux.just(saved!!) }
        assertEquals(action.operationId, loader.loadFinancialAction(1L)!!.operationId)
        assertEquals(action.operationId, loader.loadReadyToProcess(0, 10).single().operationId)
        assertEquals(action.operationId, loader.loadRetries(10).single().operationId)
        every { repository.save(any<FinancialActionModel>()) } answers {
            saved = firstArg()
            Mono.just(saved!!)
        }
        persister.persistWithStatus(action, FinancialActionStatus.PROCESSED)
        assertEquals(action.operationId, saved!!.operationId)
        assertEquals(FinancialActionStatus.PROCESSED, saved!!.status)
    }
}
