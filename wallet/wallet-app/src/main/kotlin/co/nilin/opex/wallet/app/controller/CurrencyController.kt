package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.CurrencyServiceV2
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
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


    @PostMapping("/currency/{currencyUuid}/imp")
    suspend fun addImp2Currency(@PathVariable("currencyUuid") currencyUuid: String,
                                @RequestBody request: CryptoCurrencyCommand): CurrencyCommand? {
        return currencyService.addImp2Currency(request.apply { this.currencyUUID = currencyUUID })
    }


//    @GetMapping("/currency/{symbol}")
//    suspend fun getCurrency(@PathVariable("symbol") symbol: String): Currency? {
//        return currencyService.fetchCurrency(symbol)
//    }
//
//    @GetMapping("/currency")
//    suspend fun getCurrencies(): Currencies? {
//        return currencyService.fetchCurrencies()
//    }

}