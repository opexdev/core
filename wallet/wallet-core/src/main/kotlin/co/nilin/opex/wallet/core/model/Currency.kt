package co.nilin.opex.wallet.core.model

import java.math.BigDecimal

data class Currency(val symbol: String, val name: String, val precision: BigDecimal)