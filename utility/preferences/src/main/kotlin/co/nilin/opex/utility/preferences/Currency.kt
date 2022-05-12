package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Currency(
    var symbol: String = "",
    var name: String = "",
    var precision: BigDecimal = BigDecimal.ZERO,
    var leftSideFraction: BigDecimal = precision,
    var rightSideFraction: BigDecimal = precision,
    var mainBalance: BigDecimal = BigDecimal.ZERO,
    var dailyTotal: BigDecimal = BigDecimal.ZERO,
    var dailyCount: Int = 0,
    var monthlyTotal: BigDecimal = BigDecimal.ZERO,
    var monthlyCount: Int = 0,
    var implementations: List<CurrencyImplementation> = emptyList()
)
