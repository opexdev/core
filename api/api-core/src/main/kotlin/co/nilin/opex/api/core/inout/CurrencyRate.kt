package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class CurrencyRate(
    val currency: String,
    val basedOn: String,
    val rate: BigDecimal
)