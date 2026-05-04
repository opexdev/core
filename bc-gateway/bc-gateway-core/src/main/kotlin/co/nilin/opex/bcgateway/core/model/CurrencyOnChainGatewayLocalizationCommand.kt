package co.nilin.opex.bcgateway.core.model


data class CurrencyOnChainGatewayLocalizationCommand(
    var id: Long? = null,
    var depositDescription: String? = null,
    var withdrawDescription: String? = null,
    var language: String
)




