package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.engine.core.model.PersistentOrder

data class OrderBookUpdateEvent(
    val pair: Pair,
    val time: Long,
    val orders: List<PersistentOrder>
)