package co.nilin.opex.api.core.inout

data class MarketBasicData(
    val quoteCurrencies: List<String>,
    val referenceCurrencies: List<String>,
    val withdrawReferenceCurrency: String,
    val tradeReferenceCurrency: String,
)
