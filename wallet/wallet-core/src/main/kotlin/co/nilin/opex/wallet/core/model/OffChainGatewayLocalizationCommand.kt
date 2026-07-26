package co.nilin.opex.wallet.core.model


data class OffChainGatewayLocalizationCommand(
    var id: Long? = null,
    var depositDescription: String? = null,
    var withdrawDescription: String? = null,
    var language: String
)




