package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

data class PublishedEventRecord(
    val id: Long?,
    val topic: String,
    val partition: Int,
    val offset: Long,
    val key: String?,
    val payload: String,
    val eventTimestamp: LocalDateTime,
    val createdAt: LocalDateTime
)
