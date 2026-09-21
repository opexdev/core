package co.nilin.opex.price.core.dto

import java.time.LocalDateTime

data class HistoryRangeRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
