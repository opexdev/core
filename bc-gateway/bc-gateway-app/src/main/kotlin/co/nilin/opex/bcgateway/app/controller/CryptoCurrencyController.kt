package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.app.dto.ChainResponse
import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.ChainLoader
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/crypto-currency")
class CryptoCurrencyController(val cryptoCurrencyHandler: CryptoCurrencyHandlerV2, private val chainLoader: ChainLoader) {

    @PostMapping("/{currencySymbol}/gateway")
    suspend fun addNewCurrencyGateway(
            @PathVariable("currencySymbol") currencySymbol: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.createOnChainGateway(request.apply {
            this.currencySymbol = currencySymbol
        })
    }


    @PutMapping("/{currency}/gateway/{gatewayUuid}")
    suspend fun updateCurrencyGateway(
            @PathVariable("currency") currencySymbol: String,
            @PathVariable("gatewayUuid") gatewayUuid: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateOnChainGateway(request.apply {
            this.currencySymbol = currencySymbol
            this.gatewayUuid = gatewayUuid
        })
    }


    @DeleteMapping("/{currency}/gateway/{gatewayUuid}")
    suspend fun deleteCurrencyGateway(
            @PathVariable("currency") currencySymbol: String,
            @PathVariable("gatewayUuid") gatewayUuid: String,
    ):Void? {
       return cryptoCurrencyHandler.deleteOnChainGateway(
                gatewayUuid, currencySymbol)
    }

    @GetMapping("/gateways")
    suspend fun fetchGateways(@RequestParam("currency") currencySymbol: String? = null): List<CryptoCurrencyCommand>? {
        return cryptoCurrencyHandler.fetchCurrencyOnChainGateways(FetchGateways(currencySymbol = currencySymbol))
    }

    @GetMapping("/{currency}/gateways")
    suspend fun fetchCurrencyGateways(@PathVariable("currency") currencySymbol: String): List<CryptoCurrencyCommand>?? {
        return cryptoCurrencyHandler.fetchCurrencyOnChainGateways(FetchGateways(currencySymbol = currencySymbol))
    }

    @GetMapping("/{currency}/gateway/{gatewayUuid}")
    suspend fun fetchSpecificGateway(@PathVariable("gatewayUuid") gatewayUuid: String,
                                  @PathVariable("currency") currencySymbol: String): List<CryptoCurrencyCommand>? {
        return cryptoCurrencyHandler.fetchCurrencyOnChainGateways(FetchGateways(gatewayUuid = gatewayUuid, currencySymbol = currencySymbol))
    }

    @GetMapping("/chain")
    suspend fun getChains(): List<ChainResponse> {
        return chainLoader.fetchAllChains().map { c -> ChainResponse(c.name, c.addressTypes.map { it.type }) }
    }

}
