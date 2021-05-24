package co.nilin.mixchange.matching.core.inout

import co.nilin.mixchange.matching.core.model.Pair

data class OrderEditCommand(val ouid: String, val uuid: String, val orderId: Long, val pair: Pair, val price: Long, val quantity: Long)