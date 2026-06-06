package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.inout.CurrencyLocalizationCommand

data class CurrencyLocalizationRequest(
    val currencyLocalizations: List<CurrencyLocalizationCommand>
)