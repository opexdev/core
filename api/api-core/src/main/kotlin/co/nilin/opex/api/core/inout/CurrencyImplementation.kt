package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class CurrencyImplementation(
    val currency: Currency,
    val implCurrency: Currency,
    val chain: Chain,
    val token: Boolean,
    val tokenAddress: String?,
    val tokenName: String?,
    val withdrawEnabled: Boolean,
    val withdrawFee: BigDecimal,
    val withdrawMin: BigDecimal,
    val decimal: Int
)