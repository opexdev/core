package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Currency(
    var symbol: String = "",
    var name: String = "",
    var precision: BigDecimal = BigDecimal.ONE,
    var mainBalance: BigDecimal = BigDecimal.ZERO,
    var dailyTotal: BigDecimal = BigDecimal.valueOf(1000),
    var dailyCount: Int = 100,
    var monthlyTotal: BigDecimal = BigDecimal.valueOf(30000),
    var monthlyCount: Int = 3000,
    var implementations: List<CurrencyImplementation> = emptyList(),
    var gift: BigDecimal = BigDecimal.ZERO
)
