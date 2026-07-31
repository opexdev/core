package co.nilin.opex.matching.engine.app.bl

import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.factory.OrderBookFactory
import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.engine.core.model.PersistentOrderBook
import co.nilin.opex.matching.engine.core.spi.OrderBookStore
import java.util.concurrent.ConcurrentHashMap

object OrderBooks : OrderBookStore {

    private val orderBooks =
        ConcurrentHashMap<String, SimpleOrderBook>()

    fun createOrderBook(pair: String) {
        println(
            "Going to add order book: $pair, " +
                    "current order books#${orderBooks.size}"
        )

        val pairs = pair.split("_")

        require(pairs.size == 2) {
            "Invalid pair format: $pair"
        }

        val newBook = OrderBookFactory.createOrderBook(
            Pair(pairs[0], pairs[1])
        )

        val previous = orderBooks.putIfAbsent(
            pair,
            newBook
        )

        require(previous == null) {
            "$pair has an order book right now!"
        }

        println(
            "Order book: $pair added, " +
                    "current order books#${orderBooks.size}"
        )
    }

    fun reloadOrderBook(
        persistentOrderBook: PersistentOrderBook
    ) {
        val pairKey =
            "${persistentOrderBook.pair.leftSideName}_" +
                    persistentOrderBook.pair.rightSideName

        orderBooks[pairKey] =
            OrderBookFactory.createOrderBook(
                persistentOrderBook
            )
    }

    override fun lookupOrderBook(
        pair: String
    ): SimpleOrderBook {
        return orderBooks[pair]
            ?: throw IllegalArgumentException(
                "No order book for $pair"
            )
    }

    override fun replace(
        pairKey: String,
        expected: SimpleOrderBook,
        replacement: SimpleOrderBook
    ) {
        synchronized(orderBooks) {
            val current = orderBooks[pairKey]

            if (current !== expected) {
                throw OpexError.InternalServerError.exception()
            }

            orderBooks[pairKey] = replacement
        }

    }

}