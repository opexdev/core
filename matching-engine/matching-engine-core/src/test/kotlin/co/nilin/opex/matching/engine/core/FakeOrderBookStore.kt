package co.nilin.opex.matching.engine.core

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.spi.OrderBookStore

class FakeOrderBookStore(
    initialBook: SimpleOrderBook
) : OrderBookStore {

    var currentBook = initialBook
        private set

    var replaceCount = 0


    var failure: RuntimeException? = null

    override fun lookupOrderBook(
        pairKey: String
    ): SimpleOrderBook =
        currentBook

    override fun replace(
        pairKey: String,
        expected: SimpleOrderBook,
        replacement: SimpleOrderBook
    ) {
        replaceCount++

        if (currentBook !== expected) {
            throw IllegalStateException("Invalid state, Current book has changed")
        }

        failure?.let { throw it }

        currentBook = replacement
    }

}