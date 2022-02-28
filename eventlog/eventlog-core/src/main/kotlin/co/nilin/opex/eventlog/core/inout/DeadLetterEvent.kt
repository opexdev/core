package co.nilin.opex.eventlog.core.inout

import java.time.LocalDateTime

data class DeadLetterEvent(
    val originModule: String?,
    val originTopic: String?,
    val consumerGroup: String?,
    val exceptionMessage: String?,
    val exceptionStacktrace: String?,
    val exceptionClassName: String?,
    val value: String?,
    val timestamp: LocalDateTime,
)