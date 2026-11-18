package co.nilin.opex.eventlog.ports.postgres.model

import co.nilin.opex.eventlog.core.spi.PublishedEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("opex_published_events")
data class PublishedEventModel(
    @Id val id: Long?,
    val topic: String,
    @Column("partition_id")
    val partitionId: Int,
    @Column("event_offset")
    val eventOffset: Long,
    @Column("event_key")
    val eventKey: String?,
    @Column("event_payload")
    val eventPayload: String,
    @Column("event_timestamp")
    val eventTimestamp: LocalDateTime,
    @Column("create_date")
    val createDate: LocalDateTime
) : PublishedEvent
