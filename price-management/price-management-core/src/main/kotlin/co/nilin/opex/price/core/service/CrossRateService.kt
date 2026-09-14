package co.nilin.opex.price.core.service

import co.nilin.opex.price.core.dto.CrossRateSparkline
import co.nilin.opex.price.core.spi.CrossRateSparklineLoader
import java.time.LocalDateTime

class CrossRateService(
    private val sparklineLoader: CrossRateSparklineLoader,
    private val preferredBridges: List<String> = DEFAULT_PREFERRED_BRIDGES,
) {

    suspend fun sparklinesAgainst(
        refCurrency: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        points: Int,
    ): List<CrossRateSparkline> {
        val hub = preferredBridges.firstOrNull()?.takeIf { it.isNotBlank() } ?: "USDT"
        return sparklineLoader.loadSparklines(refCurrency.trim(), hub, startTime, endTime, points)
    }

    companion object {
        val DEFAULT_PREFERRED_BRIDGES = listOf("USDT", "IRT") //TODO
    }
}
