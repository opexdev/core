package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class CurrencyImplementation(
    var symbol: String = "",
    var chain: String = "",
    var withdrawEnabled: Boolean = true,
    var token: Boolean = false,
    var tokenAddress: String? = null,
    var tokenName: String? = null,
    var withdrawFee: BigDecimal = BigDecimal.valueOf(0.01),
    var withdrawMin: BigDecimal = BigDecimal.valueOf(0.01),
    var decimal: Int = 0
)
