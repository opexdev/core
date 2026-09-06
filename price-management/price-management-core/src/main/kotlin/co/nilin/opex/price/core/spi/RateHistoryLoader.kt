package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.RateHistory
import java.time.LocalDateTime

interface RateHistoryLoader {
    suspend fun loadRateHistory(symbol: String, limit: Int = 100): List<RateHistory>
    suspend fun loadLatest(symbol: String): RateHistory?
    suspend fun loadLatestForAllSymbols(): List<RateHistory>
    suspend fun loadHistory(symbol: String, startTime: LocalDateTime, endTime: LocalDateTime): List<RateHistory>
    suspend fun loadHistoryForAllSymbols(startTime: LocalDateTime, endTime: LocalDateTime): List<RateHistory>
}
