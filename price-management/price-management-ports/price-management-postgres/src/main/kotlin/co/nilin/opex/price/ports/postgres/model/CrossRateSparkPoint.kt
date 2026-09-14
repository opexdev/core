package co.nilin.opex.price.ports.postgres.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class CrossRateSparkPoint(
    val symbol: String,
    val bucketTime: LocalDateTime,
    val price: BigDecimal,
    val changePercent: BigDecimal,
    val isTrendUp: Boolean,
)
