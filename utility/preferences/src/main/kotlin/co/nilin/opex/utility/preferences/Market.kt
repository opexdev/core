package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Market(
    var leftSide: String = "",
    var rightSide: String = "",
    var feeConfigs: List<FeeConfig> = emptyList(),
    var aliases: List<Alias> = emptyList(),
    var leftSideFraction: BigDecimal? = null,
    var rightSideFraction: BigDecimal? = null
) {
    var pair: String? = null
        get() {
            return field ?: "${leftSide}_$rightSide"
        }
}
