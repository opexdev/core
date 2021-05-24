package co.nilin.mixchange.port.eventlog.postgres.model

import co.nilin.mixchange.eventlog.spi.Event
import co.nilin.mixchange.eventlog.spi.Trade
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("opex_events")
class EventModel(
    @Id var id: Long?,
    val correlationId: String,
    val ouid: String,
    val uuid: String,
    val symbol: String,
    val event: String,
    @Column("event_json") val eventJson: String,
    val agent: String,
    val ip: String,
    @Column("event_date") val eventDate: LocalDateTime,
    @Column("create_date") val createDate: LocalDateTime
) : Event