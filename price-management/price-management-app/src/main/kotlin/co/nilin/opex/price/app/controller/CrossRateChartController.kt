package co.nilin.opex.price.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.minutes
import co.nilin.opex.price.app.cache.RedisCacheHelper
import co.nilin.opex.price.app.data.SparkLineDataResponse
import co.nilin.opex.price.app.utils.createLineChart
import co.nilin.opex.price.core.dto.CrossRateSparkline
import co.nilin.opex.price.core.service.CrossRateService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/prices")
class CrossRateChartController(
    private val crossRateService: CrossRateService,
    private val redisCacheHelper: RedisCacheHelper,
) {
    private val logger = LoggerFactory.getLogger(CrossRateChartController::class.java)

    enum class Period(val code: String, val days: Long, val points: Int, val cacheTtlMinutes: Int) {
        DAILY("24h", 1, 24, 30),
        WEEKLY("7d", 7, 42, 240),
        MONTHLY("1M", 30, 30, 720);

        companion object {
            fun fromCode(code: String): Period? = values().find { it.code == code }
        }
    }
    @GetMapping("/spark-line")
    suspend fun getSparkLine(
        @RequestParam("refCurrency") refCurrency: String,
        @RequestParam("period") periodCode: String
    ): List<SparkLineDataResponse> {
        val period = Period.fromCode(periodCode)
            ?: throw OpexError.BadRequest.exception("Invalid period, expected one of ${Period.values().map { it.code }}")

        val cacheKey = "crossRateSparkline:ref:${refCurrency.lowercase()}:${period.code}"
        val sparklines: List<CrossRateSparkline> = redisCacheHelper.getOrElse(cacheKey, period.cacheTtlMinutes.minutes()) {
            val endTime = LocalDateTime.now()
            val startTime = endTime.minusDays(period.days)
            runCatching {
                crossRateService.sparklinesAgainst(refCurrency, startTime, endTime, period.points)
            }.getOrElse { e ->
                logger.error("Failed to build sparklines for refCurrency=$refCurrency period=${period.code}", e)
                emptyList()
            }
        }

        return sparklines.map { s ->
            SparkLineDataResponse(
                symbol = s.symbol,
                isTrendUp = s.isTrendUp,
                changePercent = s.changePercent,
                svgData = createLineChart(s.prices, s.times),
            )
        }
    }
}
