package co.nilin.opex.market.app.controller

import co.nilin.opex.market.app.data.SparkLineDataResponse
import co.nilin.opex.market.core.inout.CandleData
import co.nilin.opex.market.core.inout.PriceTime
import co.nilin.opex.market.core.spi.MarketQueryHandler
import createLineChart
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal


@RestController
@RequestMapping("/v1/chart")
class ChartController(private val marketQueryHandler: MarketQueryHandler) {

    enum class Period {
        DAILY, WEEKLY, MONTHLY
    }

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

    @GetMapping("/spark-line")
    suspend fun getSparkLineForSymbols(
        @RequestParam("symbols") symbols: List<String>,
        @RequestParam("period") period: Period
    ): List<SparkLineDataResponse> {
        return symbols.mapNotNull { symbol ->
            val priceData: List<PriceTime> = when (period) {
                Period.WEEKLY -> marketQueryHandler.getWeeklyPriceData(symbol)
                Period.MONTHLY -> marketQueryHandler.getMonthlyPriceData(symbol)
                Period.DAILY -> marketQueryHandler.getDailyPriceData(symbol)
            }
            if (priceData.all { it.closePrice == BigDecimal.ZERO }) return@mapNotNull null
            val isTrendUp = priceData.last().closePrice >= priceData.first().closePrice
            val svgData = createLineChart(priceData.map { it.closePrice }, priceData.map { it.closeTime })
            SparkLineDataResponse(symbol, isTrendUp, svgData)
        }
    }
}