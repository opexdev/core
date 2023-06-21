package co.nilin.opex.matching.gateway.ports.kafka.submitter.inout

import co.nilin.opex.matching.engine.core.model.Pair

class OrderCancelRequestEvent(
    ouid: String,
    uuid: String,
    pair: Pair,
    val orderId: Long
) : OrderRequestEvent(ouid, uuid, pair)