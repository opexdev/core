package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.Pair

class OrderCancelCommand(val ouid: String, val uuid: String, val orderId: Long, val pair: Pair)