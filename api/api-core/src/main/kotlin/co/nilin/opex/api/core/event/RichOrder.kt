package co.nilin.opex.api.core.event

import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import java.math.BigDecimal

data class RichOrder(
    val orderId: Long? = 0,
    val pair: String,
    val ouid: String,
    val uuid: String,
    val userLevel: String,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,
    val leftSideFraction: BigDecimal,
    val rightSideFraction: BigDecimal,
    val direction: OrderDirection,
    val constraint: MatchConstraint,
    val type: MatchingOrderType,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val executedQuantity: BigDecimal,
    val accumulativeQuoteQty: BigDecimal,
    val status: Int = 0
) : RichOrderEvent
