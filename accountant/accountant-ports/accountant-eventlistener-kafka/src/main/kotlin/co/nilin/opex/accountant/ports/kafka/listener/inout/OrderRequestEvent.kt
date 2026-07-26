package co.nilin.opex.accountant.ports.kafka.listener.inout

import co.nilin.opex.matching.engine.core.model.Pair

abstract class OrderRequestEvent(val ouid: String, val uuid: String, val pair: Pair)