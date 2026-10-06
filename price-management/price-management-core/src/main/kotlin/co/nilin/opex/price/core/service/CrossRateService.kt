package co.nilin.opex.price.core.service

import co.nilin.opex.price.core.dto.CrossRateSparkline
import co.nilin.opex.price.core.spi.CrossRateSparklineLoader
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime

class CrossRateService(
    private val sparklineLoader: CrossRateSparklineLoader,
    @Value("\${app.cross-rate.preferred-bridge}")
    private val preferredBridge: String = "USDT"
) {

    suspend fun sparklinesAgainst(
        refCurrency: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        points: Int,
    ): List<CrossRateSparkline> {
        return sparklineLoader.loadSparklines(refCurrency.trim(), preferredBridge, startTime, endTime, points)
    }
}
