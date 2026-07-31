package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.Pair

class OrderCancelCommand(
    override val ouid: String,
    override val uuid: String,
    val orderId: Long,
    override val pair: Pair
) : OrderCommand