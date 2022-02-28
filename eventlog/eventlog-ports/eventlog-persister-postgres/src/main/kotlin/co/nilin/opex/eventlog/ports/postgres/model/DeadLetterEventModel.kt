package co.nilin.opex.eventlog.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("dead_letter_events")
data class DeadLetterEventModel(
    val originModule: String?,
    val originTopic: String?,
    val consumerGroup: String?,
    val exceptionMessage: String?,
    val exceptionStacktrace: String?,
    val exceptionClassName: String?,
    val value: String?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    @Id var id: Long? = null
)