package co.nilin.opex.api.core.inout.otc

import java.math.BigDecimal

data class Rate(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal
)

data class Rates(
    var rates: List<Rate>?
)
