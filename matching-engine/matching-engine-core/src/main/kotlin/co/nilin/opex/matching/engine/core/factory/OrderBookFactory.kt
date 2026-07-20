package co.nilin.opex.matching.engine.core.factory

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.eventh.NoOpOrderBookEventSink
import co.nilin.opex.matching.engine.core.eventh.OrderBookEventSink
import co.nilin.opex.matching.engine.core.model.*

object OrderBookFactory {

    /**
     * Creates an empty committed/live order book.
     */
    fun createOrderBook(pair: Pair): SimpleOrderBook {
        return SimpleOrderBook(
            pair = pair,
            replayMode = false,
            eventSink = NoOpOrderBookEventSink
        )
    }

    /**
     * Restores a committed/live order book from a snapshot.
     */
    fun createOrderBook(
        persistentOrderBook: PersistentOrderBook
    ): SimpleOrderBook {
        val orderBook = SimpleOrderBook(
            pair = persistentOrderBook.pair,
            replayMode = true,
            eventSink = NoOpOrderBookEventSink
        )

        orderBook.rebuild(persistentOrderBook)
        orderBook.stopReplayMode()

        return orderBook
    }

    /**
     * Creates a temporary working book.
     *
     * CREATE and CANCEL commands are executed against this book.
     * Generated events are sent to the provided collecting sink.
     */
    fun createWorkingOrderBook(
        snapshot: PersistentOrderBook,
        eventSink: OrderBookEventSink
    ): SimpleOrderBook {

        val workingBook = SimpleOrderBook(
            pair = snapshot.pair,
            replayMode = true,
            eventSink = eventSink
        )

        workingBook.rebuild(snapshot)
        workingBook.stopReplayMode()

        return workingBook
    }
}