package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.CrossRateSparkline
import co.nilin.opex.price.core.spi.CrossRateSparklineLoader
import co.nilin.opex.price.ports.postgres.dao.RateHistoryRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CrossRateSparklineLoaderImpl(
    private val rateHistoryRepository: RateHistoryRepository,
) : CrossRateSparklineLoader {

    override suspend fun loadSparklines(
        refCurrency: String,
        hub: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        points: Int,
    ): List<CrossRateSparkline> {
        val rows = rateHistoryRepository
            .findCrossRateSparkPoints(refCurrency, hub, startTime, endTime, points)
            .collectList()
            .awaitFirstOrNull()
            ?: return emptyList()

        return rows.groupBy { it.symbol }
            .map { (symbol, group) ->
                val ordered = group.sortedBy { it.bucketTime }
                CrossRateSparkline(
                    symbol = symbol,
                    isTrendUp = ordered.first().isTrendUp,
                    changePercent = ordered.first().changePercent,
                    prices = ordered.map { it.price },
                    times = ordered.map { it.bucketTime },
                )
            }
            .sortedBy { it.symbol }
    }
}
