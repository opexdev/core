package co.nilin.opex.price.core.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class LatestPriceResponse(
    val symbol: String,
    val price: BigDecimal,
    val timestamp: LocalDateTime
)

data class PricePoint(
    val price: BigDecimal,
    val timestamp: LocalDateTime
)

data class PriceHistoryResponse(
    val symbol: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val items: List<PricePoint>
)

data class AllPriceHistoryResponse(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val items: List<PriceHistoryResponse>
)
