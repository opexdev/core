package co.nilin.opex.price.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.minutes
import co.nilin.opex.price.app.data.SparkLineDataResponse
import co.nilin.opex.price.app.utils.CacheHelper
import co.nilin.opex.price.app.utils.createLineChart
import co.nilin.opex.price.core.service.CrossRateService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/otc")
class OtcController(
    private val crossRateService: CrossRateService,
    private val cacheHelper: CacheHelper,
) {
    private val logger = LoggerFactory.getLogger(OtcController::class.java)

    enum class Period(val code: String, val days: Long, val points: Int, val cacheTtlMinutes: Int) {
        DAILY("24h", 1, 24, 5),
        WEEKLY("7d", 7, 42, 5),
        MONTHLY("1M", 30, 30, 5);

        companion object {
            fun fromCode(code: String): Period? = entries.find { it.code == code }
        }
    }

    @GetMapping("/spark-line")
    suspend fun getSparkLine(
        @RequestParam("refCurrency") refCurrency: String,
        @RequestParam("period") periodCode: String
    ): List<SparkLineDataResponse> {
        val period = Period.fromCode(periodCode)
            ?: throw OpexError.BadRequest.exception(
                "Invalid period, expected one of ${
                    Period.entries.map { it.code }
                }"
            )

        val cacheKey = "crossRateSparkline:rendered:ref:${refCurrency.trim().lowercase()}:${period.code}"
        return cacheHelper.getTimeBasedOrElse(cacheKey, period.cacheTtlMinutes.minutes()) {
            val endTime = LocalDateTime.now()
            val startTime = endTime.minusDays(period.days)
            val sparklines = try {
                crossRateService.sparklinesAgainst(refCurrency, startTime, endTime, period.points)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Failed to load sparklines for refCurrency=$refCurrency period=${period.code}", e)
                return@getTimeBasedOrElse emptyList()
            }

            withContext(Dispatchers.Default) {
                sparklines.map { sparkline ->
                    SparkLineDataResponse(
                        symbol = sparkline.symbol,
                        isTrendUp = sparkline.isTrendUp,
                        changePercent = sparkline.changePercent,
                        svgData = createLineChart(sparkline.prices, sparkline.times),
                    )
                }
            }
        }
    }
}
