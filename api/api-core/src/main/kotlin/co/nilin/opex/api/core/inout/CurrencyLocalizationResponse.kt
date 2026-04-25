package co.nilin.opex.api.core.inout

data class CurrencyLocalizationResponse(
    val currency: String,
    val currencyLocalizations: List<CurrencyLocalizationCommand>
)