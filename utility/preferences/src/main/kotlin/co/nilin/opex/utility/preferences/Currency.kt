package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Currency(
    var symbol: String,
    var name: String,
    var leftSideFraction: BigDecimal,
    var rightSideFraction: BigDecimal,
    var precision: BigDecimal,
    var implementations: List<CurrencyImplementation>
)