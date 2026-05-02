package co.nilin.opex.api.core.inout


data class GatewayLocalizationCommand(
    var id: Long? = null,
    var depositDescription: String? = null,
    var withdrawDescription: String? = null,
    var language: String
)




