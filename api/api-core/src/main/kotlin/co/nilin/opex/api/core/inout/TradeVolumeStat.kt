package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class TradeVolumeStat(
    var symbol: String,
    val volume: BigDecimal,
    val tradeCount: BigDecimal,
    val change: Double
)