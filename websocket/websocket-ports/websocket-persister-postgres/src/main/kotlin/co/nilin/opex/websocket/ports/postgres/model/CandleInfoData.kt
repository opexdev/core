package co.nilin.opex.websocket.ports.postgres.model

import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

data class CandleInfoData(
    @Column("open_time")
    val openTime: LocalDateTime,
    @Column("close_time")
    val closeTime: LocalDateTime,
    val open: Double?,
    val close: Double?,
    val high: Double?,
    val low: Double?,
    val volume: Double?,
    val trades: Int,
)