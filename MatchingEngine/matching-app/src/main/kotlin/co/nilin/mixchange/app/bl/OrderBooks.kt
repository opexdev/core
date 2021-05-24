package co.nilin.mixchange.app.bl

import co.nilin.mixchange.matching.core.factory.OrderBookFactory
import co.nilin.mixchange.matching.core.model.OrderBook
import co.nilin.mixchange.matching.core.model.PersistentOrderBook

object OrderBooks {
    private val orderBooks = mutableMapOf<String,OrderBook>()

    fun createOrderBook(pair: String) {
        println("Going to add order book:" + pair + ", current order books#" + orderBooks.size)
        if ( orderBooks.containsKey(pair))
            throw IllegalArgumentException("${pair} has an order book right now!")
        val pairs = pair.split("_")
        orderBooks[pair] = OrderBookFactory.createOrderBook(co.nilin.mixchange.matching.core.model.Pair(pairs[0], pairs[1]))
        println("order book:" + pair + " added, current order books#" + orderBooks.size)
    }

    fun reloadOrderBook(orderBook: PersistentOrderBook){
        orderBooks["${orderBook.pair.leftSideName}_${orderBook.pair.rightSideName}"] = OrderBookFactory.createOrderBook(orderBook)
    }

    fun lookupOrderBook(pair: String): OrderBook {
        return orderBooks[pair]?:throw IllegalArgumentException("No orderbook for $pair")
    }
}