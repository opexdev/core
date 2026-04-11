package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal


data class CurrencyExchangeRate(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal,
    val isSwappable: Boolean
)

data class CurrencyExchangeRatesResponse(val rates: List<CurrencyExchangeRate>)