package co.nilin.opex.market.ports.postgres.model

import co.nilin.opex.market.core.inout.MatchConstraint
import co.nilin.opex.market.core.inout.MatchingOrderType
import co.nilin.opex.market.core.inout.OrderDirection
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("orders")
data class OrderModel(
    @Id var id: Long?,
    val ouid: String,
    val uuid: String,
    val clientOrderId: String?,
    val symbol: String,
    val orderId: Long?,
    val makerFee: BigDecimal?,
    val takerFee: BigDecimal?,
    val leftSideFraction: BigDecimal?,
    val rightSideFraction: BigDecimal?,
    val userLevel: String?,
    @Column("side") val direction: OrderDirection?,
    @Column("match_constraint") val constraint: MatchConstraint?,
    @Column("order_type") val type: MatchingOrderType?,
    val price: BigDecimal?,
    val quantity: BigDecimal?,
    val quoteQuantity: BigDecimal?,
    val createDate: LocalDateTime?,
    val updateDate: LocalDateTime,
    @Version
    var version: Long? = null
)