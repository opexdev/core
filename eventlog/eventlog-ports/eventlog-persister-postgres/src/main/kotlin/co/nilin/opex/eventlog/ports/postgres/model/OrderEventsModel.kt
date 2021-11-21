package co.nilin.opex.eventlog.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("opex_order_events")
class OrderEventsModel(
    @Id var id: Long?,
    var ouid: String,
    val uuid: String,
    @Column("matching_orderid") val matchingOrderId: Long?,
    val price: Long?,
    val quantity: Long?,
    @Column("filled_quantity") val filledQuantity: Long?,
    val event: String,
    val agent: String,
    val ip: String,
    @Column("event_date") val eventDate: LocalDateTime,
    @Column("create_date") val createDate: LocalDateTime
)