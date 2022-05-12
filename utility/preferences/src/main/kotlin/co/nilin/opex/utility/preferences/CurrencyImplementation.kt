package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class CurrencyImplementation(
    var chain: String = "",
    var withdrawEnabled: Boolean = true,
    var token: Boolean = false,
    var tokenAddress: String? = null,
    var tokenName: String? = null,
    var withdrawFee: BigDecimal = BigDecimal.ZERO,
    var withdrawMin: BigDecimal = BigDecimal.ZERO,
    var decimal: Int = 0
)
