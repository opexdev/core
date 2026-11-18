package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

interface TradeReader {
    suspend fun findBetween(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<TradeRecord>
}
