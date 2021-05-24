package co.nilin.mixchange.accountant.core.model

import co.nilin.mixchange.matching.core.model.OrderDirection
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
    val price: Long,
    val quantity: Long,
    val filledQuantity: Long,
    val firstTransferAmount: BigDecimal,
    var remainedTransferAmount: BigDecimal,
    var status: Int,
    val id: Long? = null
)