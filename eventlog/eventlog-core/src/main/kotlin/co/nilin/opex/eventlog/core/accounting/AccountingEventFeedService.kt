package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

interface AccountingEventFeedService {
    suspend fun findEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent>
}
