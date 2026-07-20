package co.nilin.opex.matching.engine.core.model

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook

data class PreparedStateTransition(
    val baseSequence: Long,
    val nextSequence: Long,
    val snapshot: PersistentOrderBook,
    val preparedBook: SimpleOrderBook
)