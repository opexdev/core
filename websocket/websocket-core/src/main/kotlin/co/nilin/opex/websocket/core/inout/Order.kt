package co.nilin.opex.websocket.core.inout

import co.nilin.opex.websocket.core.inout.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    var id: Long,
    val ouid: String,
    val uuid: String,
    val clientOrderId: String?,
    val symbol: String,
    val orderId: Long?,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,
    val leftSideFraction: BigDecimal,
    val rightSideFraction: BigDecimal,
    val userLevel: String,
    val direction: OrderDirection,
    val constraint: MatchConstraint,
    val type: MatchingOrderType,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val executedQuantity: BigDecimal,
    val accumulativeQuoteQty: BigDecimal,
    val status: OrderStatus,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
)