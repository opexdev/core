package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.spi.PublishedEvent
import co.nilin.opex.eventlog.core.spi.PublishedEventPersister
import co.nilin.opex.eventlog.ports.postgres.dao.PublishedEventRepository
import co.nilin.opex.eventlog.ports.postgres.model.PublishedEventModel
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PublishedEventPersisterImpl(
    private val repository: PublishedEventRepository
) : PublishedEventPersister {

    override suspend fun save(
        topic: String,
        partition: Int,
        offset: Long,
        key: String?,
        payload: String,
        eventTimestamp: LocalDateTime
    ): PublishedEvent {
        val event = PublishedEventModel(
            id = null,
            topic = topic,
            partitionId = partition,
            eventOffset = offset,
            eventKey = key,
            eventPayload = payload,
            eventTimestamp = eventTimestamp,
            createDate = LocalDateTime.now()
        )
        return try {
            repository.save(event).awaitFirst()
        } catch (ex: DuplicateKeyException) {
            event
        }
    }
}
