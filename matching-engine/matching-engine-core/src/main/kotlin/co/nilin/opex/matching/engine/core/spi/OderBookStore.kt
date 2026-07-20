package co.nilin.opex.matching.engine.core.spi

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook

interface OrderBookStore {

    fun lookupOrderBook(pairKey: String): SimpleOrderBook

    fun replace(
        pairKey: String,
        expected: SimpleOrderBook,
        replacement: SimpleOrderBook
    ): Boolean

    fun swapOrderBook(
        pairKey : String,
        expected : SimpleOrderBook,
        replacement : SimpleOrderBook
    )

}