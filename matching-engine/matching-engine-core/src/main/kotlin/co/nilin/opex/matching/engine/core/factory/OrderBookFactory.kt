package co.nilin.opex.matching.engine.core.factory

import co.nilin.opex.matching.engine.core.model.OrderBook
import co.nilin.opex.matching.engine.core.model.PersistentOrderBook

object OrderBookFactory {
    fun createOrderBook(pair: co.nilin.opex.matching.engine.core.model.Pair): OrderBook {
        return co.nilin.opex.matching.engine.core.engine.SimpleOrderBook(pair, false)
    }

    fun createOrderBook(persistentOrderBook: PersistentOrderBook): OrderBook {
        val orderBook = co.nilin.opex.matching.engine.core.engine.SimpleOrderBook(persistentOrderBook.pair, true)
        orderBook.rebuild(persistentOrderBook)
        orderBook.stopReplayMode()
        return orderBook
    }
}