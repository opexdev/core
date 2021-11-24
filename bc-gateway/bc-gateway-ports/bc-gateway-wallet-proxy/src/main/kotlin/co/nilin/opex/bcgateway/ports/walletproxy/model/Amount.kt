package co.nilin.opex.bcgateway.ports.walletproxy.model

import java.math.BigDecimal

data class Amount(val currency: Currency, val amount: BigDecimal)