package co.nilin.opex.eventlog.core.accounting

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AggregatedAccountingEventFeedService(
    private val eventSources: List<AccountingEventSource>
) : AccountingEventFeedService {

    override suspend fun findEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        if (eventSources.isEmpty()) return emptyList()

        val cappedLimit = limit.coerceIn(1, 10_000)
        return coroutineScope {
            eventSources.map { source ->
                async { source.collectEvents(from, to, cappedLimit) }
            }.awaitAll()
        }
            .flatten()
            .filter { !it.occurredAt.isBefore(from) && !it.occurredAt.isAfter(to) }
            .sortedByDescending { it.occurredAt }
            .distinctBy { (it.eventId ?: it.txId) to it.source }
            .take(cappedLimit)
    }
}
