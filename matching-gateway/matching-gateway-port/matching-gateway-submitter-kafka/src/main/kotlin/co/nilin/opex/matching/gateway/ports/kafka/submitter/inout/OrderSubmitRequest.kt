package co.nilin.opex.matching.gateway.ports.kafka.submitter.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import java.util.*

data class OrderSubmitRequest(
    val uuid: String,
    val pair: Pair,
    val price: Long,
    val quantity: Long,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType,
    val ouid: String = UUID.randomUUID().toString(),
    val orderId: Long? = null,
)