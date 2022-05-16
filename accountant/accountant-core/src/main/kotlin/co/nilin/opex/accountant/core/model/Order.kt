package co.nilin.opex.accountant.core.model

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import java.math.BigDecimal

data class Order(
    val pair: String,
    val ouid: String,
    var matchingEngineId: Long?,
    val makerFee: Double,
    val takerFee: Double,
    val leftSideFraction: Double,
    val rightSideFraction: Double,
    val uuid: String,
    val userLevel: String,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType,
    val price: Long,
    val quantity: Long,
    val filledQuantity: Long,
    val origPrice: BigDecimal,
    val origQuantity: BigDecimal,
    val filledOrigQuantity: BigDecimal,
    val firstTransferAmount: BigDecimal,
    var remainedTransferAmount: BigDecimal,
    var status: Int,
    val id: Long? = null
) {

    fun isAsk() = direction == OrderDirection.ASK

    fun isBid() = direction == OrderDirection.BID
}