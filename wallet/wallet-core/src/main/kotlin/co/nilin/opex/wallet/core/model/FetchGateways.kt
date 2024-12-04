package co.nilin.opex.wallet.core.model

data class FetchGateways(
    val currencySymbol: String? = null,
    var gatewayUuid: String? = null,
    var chain: String? = null,
    var currencyImplementationName: String? = null
)