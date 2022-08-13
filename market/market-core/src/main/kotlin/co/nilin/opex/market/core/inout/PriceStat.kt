package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class PriceStat(
    val symbol: String,
    val lastPrice: BigDecimal,
    val priceChangePercent: Double
)