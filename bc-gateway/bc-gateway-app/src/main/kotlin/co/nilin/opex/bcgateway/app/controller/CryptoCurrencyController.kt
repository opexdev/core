package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/crypto-currency")
class CryptoCurrencyController(val cryptoCurrencyHandler: CryptoCurrencyHandlerV2) {

    @PostMapping("/{currencySymbol}/impl")
    suspend fun addNewCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.createImpl(request.apply {
            this.implUuid = UUID.randomUUID().toString()
            this.currencySymbol = currencySymbol
        })
    }


    @PutMapping("/{currencySymbol}/impl/{implUuid}")
    suspend fun updateCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @PathVariable("implUuid") implUuid: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateImpl(request.apply {
            this.currencySymbol = currencySymbol
            this.implUuid = implUuid
        })
    }


    @DeleteMapping("/{currencySymbol}/imp/{implUuid}")
    suspend fun deleteCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @PathVariable("implUuid") implUuid: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateImpl(request.apply {
            this.currencySymbol = currencySymbol
            this.implUuid = implUuid
        })
    }




    @GetMapping("/impls")
    suspend fun fetchCurrenciesImpls(@RequestParam("currency") currencySymbol:String?=null): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(currencySymbol = currencySymbol))
    }

    @GetMapping("/impl/{implUuid}")
    suspend fun fetchSpecificImpl(@PathVariable("implUuid") implUuid: String): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(implUuid = implUuid))
    }


    @GetMapping("/chains")
    suspend fun getNetworks(@RequestParam(required = false) currency: String?): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(currencySymbol = currency))
    }


}
