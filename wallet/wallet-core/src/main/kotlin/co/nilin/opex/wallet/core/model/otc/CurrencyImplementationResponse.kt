package co.nilin.opex.wallet.core.model.otc

import java.math.BigDecimal

data class CurrencyImplementationResponse(
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

data class Chain(val name: String)

data class Currency(val name:String, val symbol:String)

data class FetchCurrencyInfo(val currency: Currency, val implementations: List<CurrencyImplementationResponse>)