package co.nilin.opex.port.eventlog.postgres.impl

import co.nilin.opex.eventlog.spi.Event
import co.nilin.opex.eventlog.spi.EventPersister
import co.nilin.opex.matching.core.eventh.events.CoreEvent
import co.nilin.opex.matching.core.eventh.events.OneOrderEvent
import co.nilin.opex.matching.core.eventh.events.TradeEvent
import co.nilin.opex.port.eventlog.postgres.dao.EventRepository
import co.nilin.opex.port.eventlog.postgres.model.EventModel
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class EventPersisterImpl(val eventRepository: EventRepository) : EventPersister {
    override suspend fun saveEvent(event: CoreEvent): List<Event> {
        if (event is OneOrderEvent) {
            return listOf(
                eventRepository.save(
                    EventModel(
                        null,
                        UUID.randomUUID().toString(),
                        event.ouid(),
                        event.uuid(),
                        event.pair.toString(),
                        event::class.simpleName!!,
                        "",
                        "agent",
                        "127.0.0.1",
                        event.eventDate,
                        LocalDateTime.now()
                    )
                ).awaitFirst()
            )
        } else if (event is TradeEvent) {
            val correlation = UUID.randomUUID().toString()
            val tuple = eventRepository.save(
                EventModel(
                    null,
                    correlation,
                    event.takerOuid,
                    event.takerUuid,
                    event.pair.toString(),
                    event::class.simpleName!!,
                    "",
                    "agent",
                    "127.0.0.1",
                    event.eventDate,
                    LocalDateTime.now()
                )
            ).zipWhen {
                eventRepository.save(
                    EventModel(
                        null,
                        correlation,
                        event.makerOuid,
                        event.makerUuid,
                        event.pair.toString(),
                        event::class.simpleName!!,
                        "",
                        "agent",
                        "127.0.0.1",
                        event.eventDate,
                        LocalDateTime.now()
                    )
                )
            }.awaitFirst()
            return listOf(tuple.t1, tuple.t2)
        }
        TODO()
    }


}