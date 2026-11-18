package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

interface AccountingEventSource {
    suspend fun collectEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent>
}
