package co.nilin.mixchange.wallet.core.model

import java.math.BigDecimal

data class Amount(val currency: Currency, val amount: BigDecimal)