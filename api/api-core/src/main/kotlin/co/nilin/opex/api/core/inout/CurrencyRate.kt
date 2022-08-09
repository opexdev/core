package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class CurrencyRate(
    val base: String,
    val quote: String,
    val source: String,
    val rate: BigDecimal
)