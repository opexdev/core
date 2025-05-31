package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class Amount(val currency: CurrencyCommand, val amount: BigDecimal)