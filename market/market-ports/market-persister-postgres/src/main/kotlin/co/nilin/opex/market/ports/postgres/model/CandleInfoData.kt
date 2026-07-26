package co.nilin.opex.market.ports.postgres.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class CandleInfoData(
    val openTime: LocalDateTime,
    val closeTime: LocalDateTime,
    val open: BigDecimal?,
    val close: BigDecimal?,
    val high: BigDecimal?,
    val low: BigDecimal?,
    val volume: BigDecimal?,
    val trades: Int,
)