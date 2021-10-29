package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class Currency(val symbol: String, val name: String)
data class CurrencyImplementation(
    val currency: Currency,
    val chain: Chain,
    val token: Boolean,
    val tokenAddress: String?,
    val tokenName: String?,
    val withdrawEnabled: Boolean,
    val withdrawFee: BigDecimal,
    val withdrawMin: BigDecimal,
    val decimal: Int
)

data class CurrencyInfo(val currency: Currency, val implementations: List<CurrencyImplementation>)
