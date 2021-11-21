package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.Pair

data class OrderEditCommand(
    val ouid: String,
    val uuid: String,
    val orderId: Long,
    val pair: Pair,
    val price: Long,
    val quantity: Long
)