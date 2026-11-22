package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

interface AccountingEventFeedService {
    suspend fun findEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent>

    suspend fun findRawEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> = findEvents(from, to, limit)
}
