package co.nilin.opex.eventlog.core.swap

import java.time.LocalDateTime

interface SwapLogReader {
    suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<SwapRecord>
}
