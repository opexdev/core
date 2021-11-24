package co.nilin.opex.wallet.core.model

import java.math.BigDecimal

data class Amount(val currency: Currency, val amount: BigDecimal)