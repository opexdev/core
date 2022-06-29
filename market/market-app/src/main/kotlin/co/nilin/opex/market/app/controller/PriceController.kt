package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.CurrencyPrice
import co.nilin.opex.market.core.spi.MarketRateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/price")
class PriceController(private val marketRateService: MarketRateService) {

    @GetMapping
    suspend fun getPrices(@RequestParam basedOn: String): List<CurrencyPrice> {
        return marketRateService.priceOfAllCurrencies(basedOn)
    }

    @GetMapping("/{currency}")
    suspend fun getPrice(@PathVariable currency: String, @RequestParam basedOn: String): CurrencyPrice {
        return marketRateService.priceOfCurrency(currency, basedOn)
    }

}