package co.nilin.opex.eventlog.ports.kafka.listener.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair

class OrderSubmitRequestEvent(
    ouid: String,
    uuid: String,
    pair: Pair,
    val price: Long,
    val quantity: Long,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType,
    val userLevel: String,
    val orderId: Long? = null,
) : OrderRequestEvent(ouid, uuid, pair)