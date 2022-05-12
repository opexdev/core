package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class FeeConfig(
    var userLevel: String = "",
    var direction: String? = "",
    var makerFee: BigDecimal = BigDecimal.valueOf(0.01),
    var takerFee: BigDecimal = BigDecimal.valueOf(0.01)
)
