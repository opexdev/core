package co.nilin.opex.api.core.inout.otc

import java.math.BigDecimal

data class Rate(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal,
    val applicator: String? = null
)

data class Rates(
    var rates: List<Rate>?
)
