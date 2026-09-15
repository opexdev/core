package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.CrossRateSparkline
import java.time.LocalDateTime

interface CrossRateSparklineLoader {

    suspend fun loadSparklines(
        refCurrency: String,
        hub: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        points: Int,
    ): List<CrossRateSparkline>
}
