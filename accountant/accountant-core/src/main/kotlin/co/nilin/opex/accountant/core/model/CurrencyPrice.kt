package co.nilin.opex.accountant.core.model

import java.math.BigDecimal

data class CurrencyPrice(var currency: String, val buyPrice: BigDecimal? = null, var sellPrice: BigDecimal? = null)
