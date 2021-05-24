package co.nilin.mixchange.matching.core.inout

import co.nilin.mixchange.matching.core.model.Pair

class OrderCancelCommand(val ouid: String, val uuid: String, val orderId: Long, val pair: Pair)