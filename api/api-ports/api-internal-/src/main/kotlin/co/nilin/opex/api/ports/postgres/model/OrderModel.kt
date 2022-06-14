package co.nilin.opex.api.ports.postgres.model

import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("orders")
data class OrderModel(
    @Id var id: Long?,
    @Column(value = "ouid")
    val ouid: String,
    val uuid: String,
    @Column(value = "client_order_id")
    val clientOrderId: String?,
    val symbol: String,
    @Column(value = "order_id") val orderId: Long?,
    @Column("maker_fee") val makerFee: BigDecimal?,
    @Column("taker_fee") val takerFee: BigDecimal?,
    @Column("left_side_fraction") val leftSideFraction: BigDecimal?,
    @Column("right_side_fraction") val rightSideFraction: BigDecimal?,
    @Column("user_level") val userLevel: String?,
    @Column("side") val direction: OrderDirection?,
    @Column("match_constraint") val constraint: MatchConstraint?,
    @Column("order_type") val type: MatchingOrderType?,
    @Column("price") val price: BigDecimal?,
    @Column("quantity") val quantity: BigDecimal?,
    @Column("quote_quantity") val quoteQuantity: BigDecimal?,
    @Column("create_date") val createDate: LocalDateTime?,
    @Column("update_date") val updateDate: LocalDateTime,
    @Version
    @Column("version")
    var version: Long? = null
)