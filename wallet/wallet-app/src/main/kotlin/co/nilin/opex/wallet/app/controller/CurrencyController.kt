package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.CurrenciesDto
import co.nilin.opex.wallet.app.dto.CurrencyDto
import co.nilin.opex.wallet.app.service.CurrencyServiceV2
import co.nilin.opex.wallet.core.inout.CryptoCurrencyCommand
import org.springframework.web.bind.annotation.*
import java.util.UUID

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
                            @RequestParam("includeImpl") includeImpl: Boolean? = false): CurrencyDto? {

        return currencyService.fetchCurrencyWithImpls(currencySymbol, includeImpl )
    }

    @GetMapping("")
    suspend fun getCurrencies(@RequestParam("includeImpl") includeImpl: Boolean? = false): CurrenciesDto? {
        return currencyService.fetchCurrenciesWithImpls(includeImpl)
    }







    @PostMapping("/{currencySymbol}/impl")
    suspend fun addImp2Currency(@PathVariable("currencySymbol") currencySymbol: String,
                                @RequestBody request: CryptoCurrencyCommand): CurrencyDto? {
        return currencyService.addImp2Currency(request.apply {
            this.currencySymbol = currencySymbol
        })
    }

    @PutMapping("/impl/{implUuid}")
    suspend fun updateImpl(@PathVariable("implUuid") implUuid: String, @RequestBody request: CryptoCurrencyCommand): CurrencyDto? {
        return currencyService.updateImpl(request.apply { this.implUuid = implUuid })
    }

    @GetMapping("/impl/{implUuid}")
    suspend fun getImpl(@PathVariable("implUuid") implUuid: String): CryptoCurrencyCommand? {
        return currencyService.fetchCurrencyImpl(implUuid)
    }


}