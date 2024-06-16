package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyImps
import co.nilin.opex.bcgateway.core.model.FetchImpls
import co.nilin.opex.bcgateway.core.spi.CryptoCurrencyHandlerV2
import co.nilin.opex.utility.preferences.CurrencyImplementation
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/crypto-currency")
class CryptoCurrencyController(val cryptoCurrencyHandler: CryptoCurrencyHandlerV2) {

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

    @PostMapping("/{currencySymbol}/imp/{impUuid}")
    suspend fun addCurrencyInfo(
        @PathVariable("currencySymbol") currencySymbol: String,
        @PathVariable("impUuid") impUuid:String,
        @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
      return cryptoCurrencyHandler.createImpl(request.apply {
           this.currencySymbol=currencySymbol
           this.currencyImpUuid=currencyImpUuid
       })
    }

    @PutMapping("/{currencySymbol}/imp/{impUuid}")
    suspend fun updateCurrencyInfo(
            @PathVariable("currencySymbol") currencySymbol: String,
            @PathVariable("impUuid") impUuid:String,
            @RequestBody request: CryptoCurrencyCommand
    ): CryptoCurrencyCommand? {
        return cryptoCurrencyHandler.updateImpl(request.apply {
            this.currencySymbol=currencySymbol
            this.currencyImpUuid=currencyImpUuid
        })
    }


    @GetMapping("/chains")
    suspend fun getNetworks(@RequestParam(required = false) currency: String?): List<CurrencyImplementation> {
        return if (currency != null)
            currencyHandler.findImplementationsByCurrency(currency)
        else
            currencyHandler.fetchAllImplementations()
    }
}
