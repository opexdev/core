package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class PriceStat(
    var symbol: String,
    val lastPrice: BigDecimal,
    val priceChangePercent: Double
)