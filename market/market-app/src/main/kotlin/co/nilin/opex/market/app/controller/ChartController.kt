package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.CandleData
import co.nilin.opex.market.core.spi.MarketQueryHandler
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/chart")
class ChartController(private val marketQueryHandler: MarketQueryHandler) {

    @GetMapping("/{symbol}/candle")
    suspend fun getCandleDataForSymbol(
        @PathVariable symbol: String,
        @RequestParam interval: String,
        @RequestParam(required = false) since: Long?,
        @RequestParam(required = false) until: Long?,
        @RequestParam(required = false) limit: Int = 500
    ): List<CandleData> {
        return marketQueryHandler.getCandleInfo(symbol, interval, since, until, limit)
    }

}