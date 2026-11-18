package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

interface PublishedEventReader {
    suspend fun findBetween(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<PublishedEventRecord>
}
