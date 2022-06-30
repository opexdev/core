package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.spi.MarketRateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/rate")
class RateController(private val marketRateService: MarketRateService) {

    @GetMapping
    suspend fun getPrices(@RequestParam basedOn: String): List<CurrencyRate> {
        return marketRateService.currencyRate(basedOn)
    }

    @GetMapping("/{currency}")
    suspend fun getPrice(@PathVariable currency: String, @RequestParam basedOn: String): CurrencyRate {
        return marketRateService.currencyRate(currency, basedOn)
    }

}