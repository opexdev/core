package co.nilin.opex.eventlog.core.spi

import java.time.LocalDateTime

interface PublishedEventPersister {
    suspend fun save(
        topic: String,
        partition: Int,
        offset: Long,
        key: String?,
        payload: String,
        eventTimestamp: LocalDateTime
    ): PublishedEvent
}
