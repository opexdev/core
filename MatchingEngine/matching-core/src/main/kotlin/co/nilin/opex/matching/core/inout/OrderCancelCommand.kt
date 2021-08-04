package co.nilin.opex.matching.core.inout

import co.nilin.opex.matching.core.model.Pair

class OrderCancelCommand(val ouid: String, val uuid: String, val orderId: Long, val pair: Pair)