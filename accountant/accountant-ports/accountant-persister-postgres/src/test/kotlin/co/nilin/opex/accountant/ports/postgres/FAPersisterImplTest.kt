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
import io.mockk.slot
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionRetryModel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

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
        faErrorRepository
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

    @Test
    fun givenRetryableAction_whenUpdateWithError_thenScheduleUsesBackoffDelay(): Unit = runBlocking {
        val retryModel = FinancialActionRetryModel(
            faId = Valid.fa.id!!,
            nextRunTime = LocalDateTime.now(),
            retries = 0,
            isResolved = false,
            hasGivenUp = false,
            id = 10
        )
        val nextRunSlot = slot<LocalDateTime>()

        coEvery { faRetryRepository.findByFaId(Valid.fa.id!!) } returns Mono.just(retryModel)
        coEvery { faRetryRepository.scheduleNext(eq(10), eq(1), capture(nextRunSlot), eq(false)) } returns Mono.empty()
        coEvery { financialActionRepository.updateStatus(eq(Valid.fa.id!!), eq(FinancialActionStatus.RETRYING)) } returns Mono.empty()
        coEvery { faErrorRepository.save(any()) } returns Mono.empty()

        val before = LocalDateTime.now()
        faPersister.updateWithError(Valid.fa, "ERR", "message", null)

        coVerify(exactly = 1) { faRetryRepository.scheduleNext(eq(10), eq(1), any(), eq(false)) }
        assertTrue(nextRunSlot.isCaptured)
        assertTrue(nextRunSlot.captured.isAfter(before.plusSeconds(10)))
    }

}