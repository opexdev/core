package co.nilin.opex.api.core.inout.otc

import java.math.BigDecimal

data class CurrencyPrice(var currency: String, val buyPrice: BigDecimal? = null, var sellPrice: BigDecimal? = null)
