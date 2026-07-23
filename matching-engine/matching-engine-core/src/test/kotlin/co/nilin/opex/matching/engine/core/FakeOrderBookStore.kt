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
    ): Boolean {
        replaceCount++

        if (currentBook !== expected) {
            return false
        }

        failure?.let { throw it }

        if (currentBook !== expected) {
            throw IllegalStateException("Current book has changed")
        }

        currentBook = replacement
        return true
    }

}