package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.core.spi.MarketRateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/rate")
class RateController(private val marketRateService: MarketRateService) {

    @GetMapping("/{source}")
    suspend fun getRates(@PathVariable source: RateSource, @RequestParam quote: String): List<CurrencyRate> {
        return marketRateService.currencyRate(quote.uppercase(), source)
    }

    @GetMapping("/{base}/{source}")
    suspend fun getRate(
        @PathVariable source: RateSource,
        @PathVariable base: String,
        @RequestParam quote: String
    ): CurrencyRate {
        return marketRateService.currencyRate(base.uppercase(), quote.uppercase(), source)
    }

}