package co.nilin.opex.api.core.inout.otc

import java.math.BigDecimal

data class CurrencyExchangeRate(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal,
    val isSwappable: Boolean

)

data class CurrencyExchangeRatesResponse(val rates: List<CurrencyExchangeRate>)
