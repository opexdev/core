package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
import co.nilin.opex.accountant.ports.postgres.impl.FinancialActionPersisterImpl
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class FAPersisterImplTest {

    private val financialActionRepository = mockk<FinancialActionRepository> {
        coEvery { saveAll(emptyList()) } returns Flux.empty()
        coEvery { updateStatusAndIncreaseRetry(any(), any()) } returns Mono.empty()
    }
    private val faPersister = FinancialActionPersisterImpl(financialActionRepository)

    @Test
    fun givenListOfActions_whenSaving_callSaveAll(): Unit = runBlocking {
        faPersister.persist(emptyList())
        coVerify { financialActionRepository.saveAll(any() as Iterable<FinancialActionModel>) }
    }

    @Test
    fun givenFAAndStatus_whenUpdatingStatusAndFANotFound_throwException(): Unit = runBlocking {
        faPersister.updateStatus(DOC.fa, FinancialActionStatus.CREATED)
        coVerify { financialActionRepository.updateStatusAndIncreaseRetry(any(), any()) }
    }

}