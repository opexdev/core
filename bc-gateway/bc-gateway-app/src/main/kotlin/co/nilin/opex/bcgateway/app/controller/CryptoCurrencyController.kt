package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.model.*
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/crypto-currency")
class CryptoCurrencyController(val cryptoCurrencyHandler: CryptoCurrencyHandlerV2) {


    @PostMapping("/{currencySymbol}/imp")
    suspend fun addNewCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.createImpl(request.apply {
            this.impUuid = UUID.randomUUID().toString()
            this.currencySymbol = currencySymbol
        })
    }


    @PutMapping("/{currencySymbol}/imp/{impUuid}")
    suspend fun updateCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @PathVariable("impUuid") impUuid: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateImpl(request.apply {
            this.currencySymbol = currencySymbol
            this.impUuid = impUuid
        })
    }


    @DeleteMapping("/{currencySymbol}/imp/{impUuid}")
    suspend fun deleteCurrencyImpl(
            @PathVariable("currencySymbol") currencySymbol: String,
            @PathVariable("impUuid") impUuid: String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateImpl(request.apply {
            this.currencySymbol = currencySymbol
            this.impUuid = impUuid
        })
    }

    @GetMapping("/{currencySymbol}/impls")
    suspend fun fetchCurrencyImpls(@PathVariable("currencySymbol") currency: String): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(currencySymbol = currency))
    }


    @GetMapping("/impls")
    suspend fun fetchCurrenciesImpls(): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls())
    }

    @GetMapping("/impls/{implUuid}")
    suspend fun fetchSpecificImpl(@PathVariable("implUuid") implUuid: String): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(implUuid = implUuid))
    }


    @GetMapping("/chains")
    suspend fun getNetworks(@RequestParam(required = false) currency: String?): CurrencyImps? {
        return cryptoCurrencyHandler.fetchCurrencyImpls(FetchImpls(currencySymbol = currency))
    }


}
