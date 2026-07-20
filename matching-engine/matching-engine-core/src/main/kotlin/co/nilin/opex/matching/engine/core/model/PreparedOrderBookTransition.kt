package co.nilin.opex.matching.engine.core.model

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

data class PreparedOrderBookTransition(
    val commandId: String,
    val pair: Pair,

    val baseSequence: Long,
    val nextSequence: Long,

    val events: List<CoreEvent>,
    val delta: OrderBookDelta,

    val preparedBook: SimpleOrderBook
)