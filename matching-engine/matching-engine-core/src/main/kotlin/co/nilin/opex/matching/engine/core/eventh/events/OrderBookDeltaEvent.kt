package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.engine.core.model.PersistentOrder

/**
 * Describes the minimal set of changes applied to the in-memory order book as a result of processing
 * a single incoming command. It is intended to be published to a dedicated Kafka topic (compacted)
 * and used for downstream projections and/or recovery replay.
 */
data class OrderBookDeltaEvent(
    val version: Long,
    val timestamp: Long,
    val createdOrders: List<PersistentOrder>,
    val updatedOrders: List<PersistentOrder>,
    val removedOrderIds: List<Long>,
    val bestAskId: Long?,
    val bestBidId: Long?,
    val note: String? = null,
    // Keep CoreEvent contract (pair)
) : CoreEvent(Pair()) {
    constructor(
        pair: Pair,
        version: Long,
        timestamp: Long,
        createdOrders: List<PersistentOrder>,
        updatedOrders: List<PersistentOrder>,
        removedOrderIds: List<Long>,
        bestAskId: Long?,
        bestBidId: Long?,
        note: String? = null
    ) : this(
        version = version,
        timestamp = timestamp,
        createdOrders = createdOrders,
        updatedOrders = updatedOrders,
        removedOrderIds = removedOrderIds,
        bestAskId = bestAskId,
        bestBidId = bestBidId,
        note = note
    ) {
        this.pair = pair
    }
}
