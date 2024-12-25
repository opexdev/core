package co.nilin.opex.matching.engine.core.factory

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.model.OrderBook
import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.engine.core.model.PersistentOrderBook

object OrderBookFactory {

    fun createOrderBook(pair: Pair): OrderBook {
        return SimpleOrderBook(pair, false)
    }

    fun createOrderBook(persistentOrderBook: PersistentOrderBook): OrderBook {
        val orderBook = SimpleOrderBook(persistentOrderBook.pair, true)
        orderBook.rebuild(persistentOrderBook)
        orderBook.stopReplayMode()
        return orderBook
    }
}