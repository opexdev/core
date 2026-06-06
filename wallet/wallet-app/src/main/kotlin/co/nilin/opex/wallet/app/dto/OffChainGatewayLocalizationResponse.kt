package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.OffChainGatewayLocalizationCommand

data class OffChainGatewayLocalizationResponse(
    val gatewayUuid: String,
    val localizations: List<OffChainGatewayLocalizationCommand>
)
