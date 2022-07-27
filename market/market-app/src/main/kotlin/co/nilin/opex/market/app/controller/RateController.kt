package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.spi.MarketRateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/rate")
class RateController(private val marketRateService: MarketRateService) {

    @GetMapping
    suspend fun getPrices(@RequestParam basedOn: String, @RequestParam indirect: Boolean): List<CurrencyRate> {
        return if (indirect)
            marketRateService.indirectRate(basedOn.uppercase())
        else
            marketRateService.currencyRate(basedOn.uppercase())
    }

    @GetMapping("/{currency}")
    suspend fun getPrice(
        @PathVariable currency: String,
        @RequestParam basedOn: String,
        @RequestParam indirect: Boolean
    ): CurrencyRate {
        return if (indirect)
            marketRateService.indirectRate(currency.uppercase(), basedOn.uppercase())
        else
            marketRateService.currencyRate(currency.uppercase(), basedOn.uppercase())
    }

}