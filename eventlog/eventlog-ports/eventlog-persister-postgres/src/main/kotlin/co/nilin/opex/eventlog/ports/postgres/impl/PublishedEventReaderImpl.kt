package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.accounting.PublishedEventReader
import co.nilin.opex.eventlog.core.accounting.PublishedEventRecord
import co.nilin.opex.eventlog.ports.postgres.dao.PublishedEventRepository
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PublishedEventReaderImpl(
    private val repository: PublishedEventRepository
) : PublishedEventReader {
    override suspend fun findBetween(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<PublishedEventRecord> {
        val page = PageRequest.of(0, limit.coerceIn(1, 10_000))
        return repository.findByEventTimestampBetweenOrderByEventTimestampDesc(from, to, page)
            .map {
                PublishedEventRecord(
                    id = it.id,
                    topic = it.topic,
                    partition = it.partitionId,
                    offset = it.eventOffset,
                    key = it.eventKey,
                    payload = it.eventPayload,
                    eventTimestamp = it.eventTimestamp,
                    createdAt = it.createDate
                )
            }
            .collectList()
            .awaitFirst()
    }
}
