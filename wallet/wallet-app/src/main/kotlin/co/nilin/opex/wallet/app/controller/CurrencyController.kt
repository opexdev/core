package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.CurrencyServiceV2
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import co.nilin.opex.wallet.core.inout.CurrenciesCommand
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/currency")
class CurrencyController(private val currencyService: CurrencyServiceV2) {

    @PostMapping("/currency")
    suspend fun addCurrency(@RequestBody request: CurrencyCommand): CurrencyCommand? {
        return currencyService.createNewCurrency(request)
    }

    @PutMapping("/currency")
    suspend fun updateCurrency(@RequestBody request: CurrencyCommand): CurrencyCommand? {
        return currencyService.updateCurrency(request)
    }


    @PostMapping("/currency/{currencyUuid}/impl")
    suspend fun addImp2Currency(@PathVariable("currencyUuid") currencyUuid: String,
                                @RequestBody request: CryptoCurrencyCommand): CurrencyCommand? {
        return currencyService.addImp2Currency(request.apply { this.currencyUUID = currencyUUID })
    }

    @GetMapping("/currency/{currencyUuid}")
    suspend fun getCurrency(@PathVariable("currencyUuid") currencyUuid: String,
                            @RequestParam("includeImp") includeImp: Boolean? = false): CurrencyCommand? {

        return currencyService.fetchCurrencyWithImps(currencyUuid, includeImp!!)
    }

    @GetMapping("/currency")
    suspend fun getCurrencies(@RequestParam("includeImp") includeImp: Boolean? = false): CurrenciesCommand? {
        return currencyService.fetchCurrenciesWithImps(includeImp!!)
    }


    @PutMapping("/currency/impl/{implUuid}")
    suspend fun updateImpl(@PathVariable("implUuid") implUuid: String, @RequestBody request: CryptoCurrencyCommand): CurrencyCommand? {
        return currencyService.updateImp(request.apply { currencyImpUuid = implUuid })
    }


}