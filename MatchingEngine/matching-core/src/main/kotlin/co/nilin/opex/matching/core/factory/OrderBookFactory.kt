package co.nilin.opex.matching.core.factory

import co.nilin.opex.matching.core.engine.SimpleOrderBook
import co.nilin.opex.matching.core.model.OrderBook
import co.nilin.opex.matching.core.model.PersistentOrderBook

object OrderBookFactory {
    fun createOrderBook(pair: co.nilin.opex.matching.core.model.Pair): OrderBook {
        return SimpleOrderBook(pair, false)
    }

    fun createOrderBook(persistentOrderBook: PersistentOrderBook): OrderBook {
        val orderBook = SimpleOrderBook(persistentOrderBook.pair, true)
        orderBook.rebuild(persistentOrderBook)
        orderBook.stopReplayMode()
        return orderBook
    }
}