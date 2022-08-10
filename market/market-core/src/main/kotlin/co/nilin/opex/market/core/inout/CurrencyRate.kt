package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class CurrencyRate(
    val base: String,
    val quote: String,
    val source: RateSource,
    val rate: BigDecimal
)