package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.inout.CurrencyLocalizationCommand

data class CurrencyLocalizationResponse(
    val currency: String,
    val currencyLocalizations: List<CurrencyLocalizationCommand>
)