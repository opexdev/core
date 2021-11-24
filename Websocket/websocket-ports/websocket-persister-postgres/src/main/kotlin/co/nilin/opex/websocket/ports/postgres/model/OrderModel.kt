package co.nilin.opex.websocket.ports.postgres.model


import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("orders")
class OrderModel(
    @Id var id: Long?,
    @Column(value = "ouid")
    val ouid: String,
    val uuid: String,
    @Column(value = "client_order_id")
    val clientOrderId: String?,
    val symbol: String,
    @Column(value = "order_id") val orderId: Long?,
    @Column("maker_fee") val makerFee: Double?,
    @Column("taker_fee") val takerFee: Double?,
    @Column("left_side_fraction") val leftSideFraction: Double?,
    @Column("right_side_fraction") val rightSideFraction: Double?,
    @Column("user_level") val userLevel: String?,
    @Column("side") val direction: OrderDirection?,
    @Column("match_constraint") val constraint: MatchConstraint?,
    @Column("order_type") val type: OrderType?,
    @Column("price") val price: Double?,
    @Column("quantity") val quantity: Double?,
    @Column("quote_quantity") val quoteQuantity: Double?,
    @Column("executed_qty") val executedQuantity: Double?,
    @Column("accumulative_quote_qty") val accumulativeQuoteQty: Double?,
    @Column("status") val status: Int?,
    @Column("create_date") val createDate: LocalDateTime?,
    @Column("update_date") val updateDate: LocalDateTime,
    @Version
    @Column("version")
    var version: Long? = null
)