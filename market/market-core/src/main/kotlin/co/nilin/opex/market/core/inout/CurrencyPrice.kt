package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class CurrencyPrice(
    val currency: String,
    val basedOn: String,
    val price: BigDecimal
)