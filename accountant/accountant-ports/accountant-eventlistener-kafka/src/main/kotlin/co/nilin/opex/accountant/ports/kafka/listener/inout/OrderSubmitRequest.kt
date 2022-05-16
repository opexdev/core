package co.nilin.opex.accountant.ports.kafka.listener.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair

data class OrderSubmitRequest(
    val ouid: String,
    val uuid: String,
    val orderId: Long?,
    val pair: Pair,
    val price: Long = 0,
    val quantity: Long = 0,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType,
)