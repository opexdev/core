package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

class SetCurrencyExchangeRateRequest(
    val sourceSymbol: String,
    val destSymbol: String,
    val rate: BigDecimal,
    val active: Boolean
)