package co.nilin.opex.matching.engine.core.engine

import co.nilin.opex.matching.engine.core.engine.OrderBookTransitionPreparer
import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.eventh.CollectingOrderBookEventSink
import co.nilin.opex.matching.engine.core.eventh.events.CreateOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.RejectOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.model.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test
import java.util.*

class OrderBookTransitionPreparerTest {

    val pair = Pair("BTC", "USDT")
    val userId = "user-1"

    private val transitionPreparer =
        OrderBookTransitionPreparer()

    @Test
    fun givenExistingOrder_whenTransitionPrepared_thenHistoricalEventIsNotEmittedAgain() {
        runBlocking {

            val sourceBook = generateOrderBook()


            sourceBook.handleNewOrderCommand(
                generateBidCommand()
            )


            val beforeSnapShot = sourceBook.snapshot()
            val newIncomingCommand = generateBidCommand()

            val prepared = transitionPreparer.prepare(sourceBook, newIncomingCommand)

            assertEquals(beforeSnapShot.orderCounter, sourceBook.orderCounter.get())
            assertEquals(beforeSnapShot.tradeCounter, sourceBook.tradeCounter.get())
            assertEquals(beforeSnapShot.sequence, sourceBook.sequence)
            assertNotSame(sourceBook, prepared.stateTransition?.preparedBook)



            assertThat(prepared.events.filterIsInstance<CreateOrderEvent>().size).isEqualTo(1)
            assertEquals(1, prepared.events.size)
            assertThat((prepared.events.single() as CreateOrderEvent).ouid).isEqualTo(newIncomingCommand.ouid)


        }
    }

    @Test
    fun givenExistingOrder_whenTransitionPrepared_thenMatchingHappenedInWorkingBookAndSourceBookRemainsUnchanged() {
        runBlocking {
            val sourceBook = generateOrderBook()


            val existingCommand = generateAskCommand()
            sourceBook.handleNewOrderCommand(
                existingCommand
            )


            val beforeSnapShot = sourceBook.snapshot()
            val newIncomingCommand = generateBidCommand()


            val prepared = transitionPreparer.prepare(sourceBook, newIncomingCommand)

            val workingBook = prepared.stateTransition?.preparedBook
            assertEquals(beforeSnapShot.orderCounter, sourceBook.orderCounter.get())
            assertEquals(0, beforeSnapShot.tradeCounter)

            assertEquals(1, workingBook?.tradeCounter?.get())

            assertEquals(
                2, workingBook?.orders?.filter { (_, order) -> order.ouid == newIncomingCommand.ouid }?.values?.sumOf(
                    SimpleOrder::filledQuantity
                )
            )
            assertEquals(
                0, sourceBook.orders.filter { (_, order) -> order.ouid == existingCommand.ouid }.values.sumOf(
                    SimpleOrder::filledQuantity
                )
            )


            val createEvents =
                prepared.events.filterIsInstance<CreateOrderEvent>()

            val tradeEvents =
                prepared.events.filterIsInstance<TradeEvent>()

            assertThat(createEvents)
                .hasSize(1)

            assertThat(createEvents.single().ouid)
                .isEqualTo(newIncomingCommand.ouid)

            assertThat(tradeEvents)
                .hasSize(1)
        }
    }

    @Test
    fun givenExistingOrder_whenTransitionPreparedForCancellationCommand_thenSourceBookRemainsUnchanged() {
        runBlocking {
            val sourceBook = generateOrderBook()


            val existingAskCommand1 = generateAskCommand()
            val existingAskCommand2 = generateAskCommand()

            sourceBook.handleNewOrderCommand(
                existingAskCommand1
            )
            sourceBook.handleNewOrderCommand(
                existingAskCommand2
            )


            val beforeSnapShot = sourceBook.snapshot()
            val cancelCommand = generateCancelCommand(existingAskCommand1.ouid)


            val prepared = transitionPreparer.prepare(sourceBook, cancelCommand)

            val workingBook = prepared.stateTransition?.preparedBook

            assertEquals(2, sourceBook.orderCounter.get())
            assertEquals(2, beforeSnapShot.orderCounter)

            assertEquals(1, workingBook?.orders?.size)

            assertThat(sourceBook.orders.values.any { order -> order.ouid == cancelCommand.ouid }).isTrue()
            assertThat(workingBook?.orders?.values?.any { order -> order.ouid == cancelCommand.ouid }).isFalse()


        }
    }

    @Test
    fun givenExistingOrder_whenTransitionPreparedForInvalidCancellationCommand_thenEmitRejection() {
        runBlocking {
            val eventCollector = CollectingOrderBookEventSink()
            val sourceBook = generateOrderBook(eventCollector)


            val existingAskCommand = generateAskCommand()

            sourceBook.handleNewOrderCommand(
                existingAskCommand
            )


            val invalidCancelCommand = generateCancelCommand(UUID.randomUUID().toString())


            val prepared = transitionPreparer.prepare(sourceBook, invalidCancelCommand)

            val workingBookRejectionEvents = prepared.events.filterIsInstance<RejectOrderEvent>()
            val sourceBookRejectionEvents = eventCollector.events().filterIsInstance<RejectOrderEvent>()


            assertEquals(1, workingBookRejectionEvents.size)
            assertEquals(0, sourceBookRejectionEvents.size)

            assertThat(
                workingBookRejectionEvents.single().ouid
            ).isEqualTo(invalidCancelCommand.ouid)
        }
    }


    private fun generateBidCommand() = OrderCreateCommand(
        ouid = UUID.randomUUID().toString(),
        uuid = userId,
        pair = pair,
        price = 100,
        quantity = 5,
        direction = OrderDirection.BID,
        matchConstraint = MatchConstraint.GTC,
        orderType = OrderType.LIMIT_ORDER
    )

    private fun generateCancelCommand(ouid: String) = OrderCancelCommand(
        ouid = ouid,
        uuid = userId,
        pair = pair,
        orderId = Long.MAX_VALUE,
    )

    private fun generateAskCommand() = OrderCreateCommand(
        ouid = UUID.randomUUID().toString(),
        uuid = userId,
        pair = pair,
        price = 100,
        quantity = 2,
        direction = OrderDirection.ASK,
        matchConstraint = MatchConstraint.GTC,
        orderType = OrderType.LIMIT_ORDER
    )

    private fun generateOrderBook(eventCollector: CollectingOrderBookEventSink? = null) = SimpleOrderBook(
        pair = pair,
        replayMode = false,
        eventSink = eventCollector ?: CollectingOrderBookEventSink()
    )
}