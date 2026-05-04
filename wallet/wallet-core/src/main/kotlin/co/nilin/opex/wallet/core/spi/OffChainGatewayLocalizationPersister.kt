package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.OffChainGatewayLocalizationCommand

interface OffChainGatewayLocalizationPersister {

    suspend fun save(
        gatewayUuid: String,
        localizations: List<OffChainGatewayLocalizationCommand>
    ): List<OffChainGatewayLocalizationCommand>

    suspend fun fetch(gatewayUuid: String): List<OffChainGatewayLocalizationCommand>
    suspend fun delete(id: Long)
}






