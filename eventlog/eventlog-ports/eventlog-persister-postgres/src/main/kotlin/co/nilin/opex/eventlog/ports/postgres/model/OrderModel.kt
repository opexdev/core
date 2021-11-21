package co.nilin.opex.eventlog.ports.postgres.model

import co.nilin.opex.eventlog.core.spi.Order
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("opex_orders")
class OrderModel(
    @Id var id: Long?,
    val ouid: String,
    val symbol: String,
    val direction: String,
    @Column("match_constraint") val matchConstraint: String,
    @Column("order_type") val orderType: String,
    val uuid: String,
    val agent: String,
    val ip: String,
    @Column("order_date") val orderDate: LocalDateTime,
    @Column("create_date") val createDate: LocalDateTime
) : Order