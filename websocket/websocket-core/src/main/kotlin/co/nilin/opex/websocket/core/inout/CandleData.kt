package co.nilin.opex.websocket.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class CandleData(
    val openTime: LocalDateTime,
    val closeTime: LocalDateTime,
    val open: BigDecimal,
    val close: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val volume: BigDecimal,
    val quoteAssetVolume: BigDecimal,
    val trades: Int,
    val takerBuyBaseAssetVolume: BigDecimal,
    val takerBuyQuoteAssetVolume: BigDecimal,
)
