package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

data class CurrencyPrice(var currency: String, val buyPrice: BigDecimal? = null, var sellPrice: BigDecimal? = null)
