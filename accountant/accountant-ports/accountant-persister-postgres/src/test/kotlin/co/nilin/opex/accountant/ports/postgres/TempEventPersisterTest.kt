package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.ports.postgres.dao.TempEventRepository
import co.nilin.opex.accountant.ports.postgres.impl.TempEventPersisterImpl
import co.nilin.opex.accountant.ports.postgres.model.TempEventModel
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class TempEventPersisterTest {

    private val tempEventRepository = mockk<TempEventRepository> {
        every { save(any()) } returns Mono.empty()
        every { findAll(any()) } returns flow { emit(Valid.tempEventModel) }
        every { findByOuid(any()) } returns flow { emit(Valid.tempEventModel) }
        every { delete(any()) } returns Mono.empty()
        every { deleteAll(any() as Iterable<TempEventModel>) } returns Mono.empty()
        every { deleteByOuid(any()) } returns Mono.empty()
    }
    private val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val persister = TempEventPersisterImpl(tempEventRepository, objectMapper)

    @Test
    fun givenOuidAndEvent_whenSaving_callRepoOnce(): Unit = runBlocking {
        persister.saveTempEvent("event_1", Valid.testEvent)
        verify(exactly = 1) { tempEventRepository.save(any()) }
    }

    @Test
    fun givenOUID_whenLoadingTempEvent_parseEventJSON(): Unit = runBlocking {
        val events = persister.loadTempEvents("event_1")

        assertThat(events).isNotEmpty

        with(events[0]) {
            assertThat(this).isInstanceOf(CoreEvent::class.java)
            assertThat(pair.rightSideName).isEqualTo(Valid.testEvent.rightSidePair)
            assertThat(pair.leftSideName).isEqualTo(Valid.testEvent.leftSidePair)
        }
    }

    @Test
    fun givenOuid_whenDeletingByOUID_callRepoOnce(): Unit = runBlocking {
        persister.removeTempEvents(Valid.tempEvent.ouid)
        verify(exactly = 1) { tempEventRepository.deleteByOuid(eq(Valid.tempEvent.ouid)) }
    }

}