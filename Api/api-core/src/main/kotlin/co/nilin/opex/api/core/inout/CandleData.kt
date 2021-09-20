package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class CandleData(
    val openTime: LocalDateTime,
    val closeTime: LocalDateTime,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
    val quoteAssetVolume: Double,
    val trades: Int,
    val takerBuyBaseAssetVolume: Double,
    val takerBuyQuoteAssetVolume: Double,
)
