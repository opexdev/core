package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair


data class OrderCreateCommand(
    override val ouid: String,
    override val uuid: String,
    override val pair: Pair,
    val price: Long,
    val quantity: Long,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType
): OrderCommand