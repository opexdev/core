package co.nilin.opex.api.core.inout

data class GatewayLocalizationResponse(
    val gatewayUuid: String,
    val localizations: List<GatewayLocalizationCommand>
)
