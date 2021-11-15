package co.nilin.opex.matching.core.engine

import co.nilin.opex.matching.core.eventh.EventDispatcher
import co.nilin.opex.matching.core.eventh.events.OrderBookPublishedEvent
import co.nilin.opex.matching.core.inout.OrderCancelCommand
import co.nilin.opex.matching.core.inout.OrderCreateCommand
import co.nilin.opex.matching.core.inout.OrderEditCommand
import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import co.nilin.opex.matching.core.model.PersistentOrderBook
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class OrderBookEventEmitsUnitTest {
    val pair = co.nilin.opex.matching.core.model.Pair("BTC", "USDT")
    val uuid = UUID.randomUUID().toString()

    var persistentOrderBook: PersistentOrderBook? = null

    @BeforeEach
    fun setup() {
        val localHandler: (OrderBookPublishedEvent) -> Unit = {
            persistentOrderBook = it.persistentOrderBook
        }
        EventDispatcher.register(OrderBookPublishedEvent::class.java, localHandler)
    }

    @Test
    fun givenOrderBook_whenOrderCreated_thenOrderBookEventPublished() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
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
    fun givenOrderBook_whenCancelOrder_thenOrderBookEventPublished() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
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


    @Test
    fun givenOrderBook_whenEditOrder_thenOrderBookEventPublished() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                2,
                1,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        val secondOrder = orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                2,
                3,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
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
        persistentOrderBook = null
        //when
        orderBook.handleEditCommand(
            OrderEditCommand(
                UUID.randomUUID().toString(),
                uuid,
                secondOrder!!.id()!!,
                pair,
                3,
                2
            )
        )
        //then
        Assertions.assertNotNull(persistentOrderBook)
    }


}