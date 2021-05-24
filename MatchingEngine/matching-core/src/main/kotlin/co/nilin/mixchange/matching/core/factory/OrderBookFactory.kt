package co.nilin.mixchange.matching.core.factory

import co.nilin.mixchange.matching.core.engine.SimpleOrderBook
import co.nilin.mixchange.matching.core.model.OrderBook
import co.nilin.mixchange.matching.core.model.PersistentOrderBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OrderBookFactory {
    fun createOrderBook(pair: co.nilin.mixchange.matching.core.model.Pair): OrderBook {
        return SimpleOrderBook(pair, false)
    }

    fun createOrderBook(persistentOrderBook: PersistentOrderBook): OrderBook {
        val orderBook = SimpleOrderBook(persistentOrderBook.pair, true)
        orderBook.rebuild(persistentOrderBook)
        orderBook.stopReplayMode()
        return orderBook
    }
}