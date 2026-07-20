package co.nilin.opex.matching.engine.core.model


data class OrderBookDelta(
    val commandId: String,
    val pair: Pair,

    val baseSequence: Long,
    val nextSequence: Long,

    val upsertedOrders: List<PersistentOrder>,
    val removedOrderIds: Set<Long>,

    val orderCounter: Long,
    val tradeCounter: Long,
    val lastOrder: PersistentOrder?
)