package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.CandleData
import co.nilin.opex.market.core.spi.MarketQueryHandler
import createLineChart
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream

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

    @GetMapping("/{symbol}/spark-line")
    suspend fun getSparkLineForSymbol(
        @PathVariable symbol: String
    ): ResponseEntity<InputStreamResource> {
        val priceData = marketQueryHandler.getWeeklyPriceData(symbol)
        val image: ByteArrayInputStream =
            createLineChart(priceData.map { it.closePrice }, priceData.map { it.closeTime })
        return ResponseEntity
            .ok()
            .contentType(MediaType.valueOf("image/svg+xml"))
            .body(InputStreamResource(image))
    }
}