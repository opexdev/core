package co.nilin.opex.accountant.ports.walletproxy.data

import java.math.BigDecimal

data class Amount(val currency: Currency, val amount: BigDecimal)