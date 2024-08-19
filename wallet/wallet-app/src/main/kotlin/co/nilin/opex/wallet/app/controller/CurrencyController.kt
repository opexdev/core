package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.service.CurrencyServiceV2
import co.nilin.opex.wallet.core.inout.CurrencyGatewayCommand
import co.nilin.opex.wallet.core.inout.OnChainGatewayCommand
import co.nilin.opex.wallet.core.inout.CurrencyGateways
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/currency")
class CurrencyController(private val currencyService: CurrencyServiceV2) {

    @PostMapping("")
    suspend fun addCurrency(@RequestBody request: CurrencyDto): CurrencyDto? {
        return currencyService.createNewCurrency(request)
    }

    @PutMapping("/{currencySymbol}")
    suspend fun updateCurrency(@PathVariable("currencySymbol") currencySymbol: String,
                               @RequestBody request: CurrencyDto): CurrencyDto? {
        return currencyService.updateCurrency(request.apply { symbol = currencySymbol })
    }


    @GetMapping("/{currencySymbol}")
    suspend fun getCurrency(@PathVariable("currencySymbol") currencySymbol: String,
                            @RequestParam("includeGateways") includeGateway: Boolean? = false): CurrencyDto? {

        return currencyService.fetchCurrencyWithGateways(currencySymbol, includeGateway)
    }

    @GetMapping("")
    suspend fun getCurrencies(@RequestParam("includeGateways") includeGateway: Boolean? = false): CurrenciesDto? {
        return currencyService.fetchCurrenciesWithGateways(includeGateway)
    }


    @PostMapping("/{currencySymbol}/gateway")
    suspend fun addImp2Currency(@PathVariable("currencySymbol") currencySymbol: String,
                                @RequestBody request: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        return currencyService.addGateway2Currency(request.apply {
            this.currencySymbol = currencySymbol
        })
    }

    @PutMapping("{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun updateGateway(@PathVariable("gatewayUuid") gatewayUuid: String,
                              @PathVariable("currencySymbol") currencySymbol: String,
                              @RequestBody request: CurrencyGatewayCommand): CurrencyGatewayCommand? {
        return currencyService.updateGateway(request.apply {
            this.currencySymbol = currencySymbol
            this.gatewayUuid = gatewayUuid
        })
    }

    @GetMapping("{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun getGateway(@PathVariable("gatewayUuid") gatewayUuid: String,
                           @PathVariable("currencySymbol") currencySymbol: String): CurrencyGatewayCommand? {
        return currencyService.fetchCurrencyGateway(gatewayUuid, currencySymbol)
    }

    @DeleteMapping("{currencySymbol}/gateway/{gatewayUuid}")
    suspend fun deleteGateway(@PathVariable("gatewayUuid") gatewayUuid: String,
                              @PathVariable("currencySymbol") currencySymbol: String) {
        currencyService.deleteGateway(gatewayUuid, currencySymbol)
    }


    @GetMapping("/gateways")
    suspend fun getGateways(): CurrencyGateways? {
        return currencyService.fetchGateways()
    }

}