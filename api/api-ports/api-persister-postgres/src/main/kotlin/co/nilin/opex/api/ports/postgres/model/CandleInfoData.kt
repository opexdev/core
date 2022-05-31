package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.relational.core.mapping.Column
import java.math.BigDecimal
import java.time.LocalDateTime

data class CandleInfoData(
    @Column("open_time")
    val openTime: LocalDateTime,
    @Column("close_time")
    val closeTime: LocalDateTime,
    val open: BigDecimal?,
    val close: BigDecimal?,
    val high: BigDecimal?,
    val low: BigDecimal?,
    val volume: BigDecimal?,
    val trades: Int,
)