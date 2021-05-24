package co.nilin.mixchange.port.accountant.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("temp_events")
data class TempEventModel(@Id val id: Long?
, val ouid: String
, @Column("event_type") val eventType: String
, @Column("event_body") val eventBody: String
, @Column("event_date") val eventDate: LocalDateTime) {
}