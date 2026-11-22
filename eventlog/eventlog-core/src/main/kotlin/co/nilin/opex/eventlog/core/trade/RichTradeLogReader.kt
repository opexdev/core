package co.nilin.opex.eventlog.core.trade

import java.time.LocalDateTime

interface RichTradeLogReader {
    suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<RichTradeRecord>
}
