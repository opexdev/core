package co.nilin.opex.bcgateway.app.controller

import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo
import co.nilin.opex.bcgateway.core.spi.CurrencyHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/currency")
class CurrencyController(val currencyHandler: CurrencyHandler) {

    @GetMapping("/{currency}")
    suspend fun fetchCurrencyInfo(@PathVariable("currency") currency: String): CurrencyInfo {
        return currencyHandler.fetchCurrencyInfo(currency)
    }

    @GetMapping("/chains")
    suspend fun getNetworks(@RequestParam(required = false) currency: String?): List<CurrencyImplementation> {
        return if (currency != null)
            currencyHandler.findImplementationsByCurrency(currency)
        else
            currencyHandler.fetchAllImplementations()
    }
}
