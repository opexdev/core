package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionErrorRepository
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRetryRepository
import co.nilin.opex.accountant.ports.postgres.impl.FinancialActionPersisterImpl
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class FAPersisterImplTest {

    private val financialActionRepository = mockk<FinancialActionRepository> {
        coEvery { saveAll(any() as Iterable<FinancialActionModel>) } returns Flux.just(Valid.faModel)
        coEvery { updateBatchStatus(any(), any()) } returns Mono.empty()
    }
    private val faRetryRepository = mockk<FinancialActionRetryRepository>()
    private val faErrorRepository = mockk<FinancialActionErrorRepository>()

    private val faPersister = FinancialActionPersisterImpl(
        financialActionRepository,
        faRetryRepository,
        faErrorRepository,
        JsonMapperTestImpl()
    )

    @Test
    fun givenListOfActions_whenSaving_callSaveAll(): Unit = runBlocking {
        faPersister.persist(listOf(Valid.fa))
        coVerify { financialActionRepository.saveAll(eq(listOf(Valid.faModel))) }
    }

    @Test
    fun givenFAAndStatus_whenUpdatingStatusAndFANotFound_throwException(): Unit = runBlocking {
        coEvery { financialActionRepository.updateStatus(eq(Valid.fa.id!!), any()) } returns Mono.empty()
        faPersister.updateStatus(Valid.fa, FinancialActionStatus.CREATED)
        coVerify {
            financialActionRepository.updateStatus(
                eq(Valid.fa.id!!),
                eq(FinancialActionStatus.CREATED)
            )
        }
    }

}