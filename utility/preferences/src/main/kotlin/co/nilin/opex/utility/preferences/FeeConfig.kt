package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class FeeConfig(
    var direction: String, var makerFee: BigDecimal, var takerFee: BigDecimal, var userLevel: BigDecimal
)
