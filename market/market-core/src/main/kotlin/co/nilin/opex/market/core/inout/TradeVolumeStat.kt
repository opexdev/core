package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class TradeVolumeStat(
    val symbol: String,
    val volume: BigDecimal,
    val tradeCount: BigDecimal,
    val change: Double
)