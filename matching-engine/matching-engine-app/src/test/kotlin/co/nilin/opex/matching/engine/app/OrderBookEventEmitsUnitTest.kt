package co.nilin.opex.matching.engine.app

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.eventh.CollectingOrderBookEventSink
import co.nilin.opex.matching.engine.core.eventh.EventDispatcher
import co.nilin.opex.matching.engine.core.eventh.events.OrderBookPublishedEvent
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.PersistentOrderBook
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class OrderBookEventEmitsUnitTest {
    private val pair = co.nilin.opex.matching.engine.core.model.Pair("BTC", "USDT")
    private val uuid = UUID.randomUUID().toString()

    private var persistentOrderBook: PersistentOrderBook? = null

    @BeforeEach
    fun setup() {
        val localHandler: (OrderBookPublishedEvent) -> Unit = {
            persistentOrderBook = it.persistentOrderBook
        }
        EventDispatcher.register(OrderBookPublishedEvent::class.java, localHandler)
    }

    @Test
    fun givenOrderBook_whenOrderCreated_thenOrderBookEventPublished() : Unit = runBlocking {
        //given
        val orderBook = SimpleOrderBook(pair, false , CollectingOrderBookEventSink())
        //when
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                1,
                1,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        //then
        Assertions.assertNotNull(persistentOrderBook)
    }


    @Test
    fun givenOrderBook_whenCancelOrder_thenOrderBookEventPublished():Unit = runBlocking{
        //given
        val orderBook = SimpleOrderBook(pair, false , CollectingOrderBookEventSink())
        val firstOrderId = UUID.randomUUID().toString()
        val secondOrderId = UUID.randomUUID().toString()

        val firstOrder = orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                firstOrderId,
                uuid,
                pair,
                2,
                1,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                secondOrderId,
                uuid,
                pair,
                1,
                1,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        persistentOrderBook = null
        //when
        orderBook.handleCancelCommand(OrderCancelCommand(firstOrderId, uuid, firstOrder!!.id()!!, pair))
        //then
        Assertions.assertNotNull(persistentOrderBook)
    }


}