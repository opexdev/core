package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.service.CurrencyService
import co.nilin.opex.wallet.core.model.Currencies
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.CurrencyImp
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/currency")
class CurrencyController (private val currencyService: CurrencyService){

    @PostMapping("/currency")
    suspend fun addCurrency(@RequestBody request: CurrencyImp): Currency? {
        return currencyService.addCurrency(request)
    }

    @PutMapping("/currency")
    suspend fun updateCurrency(@RequestBody request: CurrencyImp): Currency? {
        return currencyService.updateCurrency(request)
    }

    @GetMapping("/currency/{symbol}")
    suspend fun getCurrency(@PathVariable("symbol") symbol: String): Currency? {
        return currencyService.fetchCurrency(symbol)
    }

    @GetMapping("/currency")
    suspend fun getCurrencies(): Currencies? {
        return currencyService.fetchCurrencies()
    }

}