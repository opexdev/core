package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair


data class OrderCreateCommand(
    val ouid: String,
    val uuid: String,
    val pair: Pair,
    val price: Long,
    val quantity: Long,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType
)