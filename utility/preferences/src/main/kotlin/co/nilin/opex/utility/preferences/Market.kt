package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Market(
    var pair: String,
    var aliases: List<String>,
    var leftSide: String,
    var rightSide: String,
    var leftSideFraction: BigDecimal?,
    var rightSideFraction: BigDecimal?,
    var feeConfigs: List<FeeConfig>
)
