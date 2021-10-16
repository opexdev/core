package co.nilin.opex.port.bcgateway.walletproxy.model

import java.math.BigDecimal

data class Amount(val currency: Currency, val amount: BigDecimal)