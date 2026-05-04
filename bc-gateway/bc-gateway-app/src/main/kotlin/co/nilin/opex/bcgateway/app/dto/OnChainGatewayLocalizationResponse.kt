package co.nilin.opex.bcgateway.app.dto

import co.nilin.opex.bcgateway.core.model.CurrencyOnChainGatewayLocalizationCommand

data class OnChainGatewayLocalizationResponse(
    val gatewayUuid: String,
    val localizations: List<CurrencyOnChainGatewayLocalizationCommand>
)
