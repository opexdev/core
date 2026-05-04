package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.OffChainGatewayLocalizationResponse
import co.nilin.opex.wallet.core.model.OffChainGatewayLocalizationCommand
import co.nilin.opex.wallet.core.spi.OffChainGatewayLocalizationPersister
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/offchain-gateway")
class OffChainGatewayLocalizationController(
    private val offChainGatewayLocalizationPersister: OffChainGatewayLocalizationPersister,
) {

    @GetMapping("/{gatewayUuid}/localization")
    suspend fun getGatewayLocalizations(@PathVariable("gatewayUuid") gatewayUuid: String): OffChainGatewayLocalizationResponse {
        val localizations = offChainGatewayLocalizationPersister.fetch(gatewayUuid)
        return OffChainGatewayLocalizationResponse(gatewayUuid, localizations)
    }

    @PostMapping("/{gatewayUuid}/localization")
    suspend fun saveGatewayLocalizations(
        @PathVariable("gatewayUuid") gatewayUuid: String,
        @RequestBody gatewayLocalizations: List<OffChainGatewayLocalizationCommand>
    ): OffChainGatewayLocalizationResponse {
        val localizations = offChainGatewayLocalizationPersister.save(gatewayUuid, gatewayLocalizations)
        return OffChainGatewayLocalizationResponse(gatewayUuid, localizations)
    }

    @DeleteMapping("/localization/{id}")
    suspend fun deleteGatewayLocalization(@PathVariable("id") id: Long) {
        offChainGatewayLocalizationPersister.delete(id)
    }
}