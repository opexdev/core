package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class CurrencyImplementation(
    var chain: String,
    var token: Boolean,
    var tokenAddress: String?,
    var tokenName: String?,
    var withdrawEnabled: Boolean,
    var withdrawFee: BigDecimal,
    var withdrawMin: BigDecimal,
    var decimal: BigDecimal
)