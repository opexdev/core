package co.nilin.opex.matching.engine.core.engine

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.eventh.CollectingOrderBookEventSink
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import kotlinx.coroutines.runBlocking
import co.nilin.opex.matching.engine.core.model.Pair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

class SimpleOrderBookSnapshotTest {
    private val pair = Pair("BTC", "USDT")
    private val userId = "user-1"

    @Test
    fun givenOrderBook_whenSnapshotRebuilt_thenCanonicalStateIsRestored(): Unit =
        runBlocking {
            val sourceBook = SimpleOrderBook(
                pair = pair,
                replayMode = false,
                eventSink = CollectingOrderBookEventSink()
            )

            sourceBook.handleNewOrderCommand(
                OrderCreateCommand(
                    ouid = "bid-1",
                    uuid = userId,
                    pair = pair,
                    price = 100,
                    quantity = 5,
                    direction = OrderDirection.BID,
                    matchConstraint = MatchConstraint.GTC,
                    orderType = OrderType.LIMIT_ORDER
                )
            )

            sourceBook.handleNewOrderCommand(
                OrderCreateCommand(
                    ouid = "ask-1",
                    uuid = userId,
                    pair = pair,
                    price = 110,
                    quantity = 3,
                    direction = OrderDirection.ASK,
                    matchConstraint = MatchConstraint.GTC,
                    orderType = OrderType.LIMIT_ORDER
                )
            )

            sourceBook.sequence = 12

            val snapshot = sourceBook.snapshot()

            val restoredBook = SimpleOrderBook(
                pair = pair,
                replayMode = true,
                eventSink = CollectingOrderBookEventSink()
            )

            restoredBook.rebuild(snapshot)
            restoredBook.stopReplayMode()

            assertNotSame(sourceBook, restoredBook)
            assertEquals(sourceBook.sequence, restoredBook.sequence)
            assertEquals(
                sourceBook.orderCounter.get(),
                restoredBook.orderCounter.get()
            )
            assertEquals(
                sourceBook.tradeCounter.get(),
                restoredBook.tradeCounter.get()
            )

            assertEquals(
                sourceBook.orders.size,
                restoredBook.orders.size
            )

            assertEquals(
                sourceBook.bidOrders.entriesList().size,
                restoredBook.bidOrders.entriesList().size
            )

            assertEquals(
                sourceBook.askOrders.entriesList().size,
                restoredBook.askOrders.entriesList().size
            )

            assertNotNull(restoredBook.bestBidOrder)
            assertNotNull(restoredBook.bestAskOrder)

            assertEquals(
                sourceBook.bestBidOrder!!.price,
                restoredBook.bestBidOrder!!.price
            )

            assertEquals(
                sourceBook.bestAskOrder!!.price,
                restoredBook.bestAskOrder!!.price
            )

            assertEquals(
                sourceBook.lastOrder!!.ouid,
                restoredBook.lastOrder!!.ouid
            )
        }
}