package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class PriceChangeResponse(
    val symbol: String,
    val priceChange: BigDecimal = BigDecimal.ZERO,
    val priceChangePercent: BigDecimal = BigDecimal.ZERO,
    val weightedAvgPrice: BigDecimal = BigDecimal.ZERO,
    val lastPrice: BigDecimal = BigDecimal.ZERO,
    val lastQty: BigDecimal = BigDecimal.ZERO,
    val bidPrice: BigDecimal = BigDecimal.ZERO,
    val askPrice: BigDecimal = BigDecimal.ZERO,
    val openPrice: BigDecimal = BigDecimal.ZERO,
    val highPrice: BigDecimal = BigDecimal.ZERO,
    val lowPrice: BigDecimal = BigDecimal.ZERO,
    val volume: BigDecimal = BigDecimal.ZERO,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long = 0,
    val lastId: Long = 0,
    val count: Long = 0,
)
