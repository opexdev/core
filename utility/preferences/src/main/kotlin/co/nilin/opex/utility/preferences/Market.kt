package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Market(
    var leftSide: String = "",
    var rightSide: String = "",
    var pair: String? = null,
    var feeConfigs: List<FeeConfig> = emptyList(),
    var aliases: List<Alias> = emptyList(),
    var leftSideFraction: BigDecimal? = null,
    var rightSideFraction: BigDecimal? = null
)
