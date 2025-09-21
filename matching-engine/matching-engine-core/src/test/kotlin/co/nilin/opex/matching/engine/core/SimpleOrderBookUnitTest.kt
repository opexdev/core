package co.nilin.opex.matching.engine.core

import co.nilin.opex.matching.engine.core.engine.SimpleOrderBook
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.inout.OrderEditCommand
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.SimpleOrder
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class SimpleOrderBookUnitTest {
    private val pair = co.nilin.opex.matching.engine.core.model.Pair("BTC", "USDT")
    private val ETH_BTC_PAIR = co.nilin.opex.matching.engine.core.model.Pair("ETH", "BTC")
    private val uuid = UUID.randomUUID().toString()

    @Test
    fun givenEmptyOrderBook_whenGtcBidLimitOrderCreated_then1BucketWithSize1() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        //when
        val order = orderBook.handleNewOrderCommand(
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
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 1)
        Assertions.assertEquals(orderBook.bestBidOrder, order)
        Dispatchers.Default
    }

    @Test
    fun givenOrderBookWithBidOrders_whenGtcBidLimitOrderWithSamePriceCreated_then() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
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
        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
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
            ) as SimpleOrder
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 1)
        Assertions.assertEquals(orderBook.bestBidOrder, bestBidOrder)
        Assertions.assertEquals(bestBidOrder!!.worse, order)
        Assertions.assertEquals(order.better, bestBidOrder)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).lastOrder, order)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).totalQuantity, 2)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).ordersCount, 2)
    }

    @Test
    fun givenOrderBookWithBidOrders_whenGtcBidLimitOrderWithLowerPriceCreated_thenBestOrderNotChange() {
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
        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
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
            ) as SimpleOrder
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 2)
        Assertions.assertEquals(orderBook.bestBidOrder, bestBidOrder)
        Assertions.assertEquals(bestBidOrder!!.worse, order)
        Assertions.assertEquals(order.better, bestBidOrder)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).lastOrder, order)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).totalQuantity, 1)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).ordersCount, 1)
    }

    @Test
    fun givenOrderBookWithBidOrders_whenGtcBidLimitOrderWithHigherPriceCreated_thenBestOrderChanged() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
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
        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
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
            ) as SimpleOrder
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 2)
        Assertions.assertEquals(orderBook.bestBidOrder, order)
        Assertions.assertEquals(bestBidOrder!!.better, order)
        Assertions.assertEquals(order.worse, bestBidOrder)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).lastOrder, order)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).totalQuantity, 1)
        Assertions.assertEquals(orderBook.bidOrders.get(order.price!!).ordersCount, 1)
    }

    @Test
    fun givenOrderBookWithBidOrders_whenGtcAskLimitOrderWithSamePriceCreated_thenInstantMatch() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
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
        //when
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                1,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 0)
        Assertions.assertEquals(orderBook.askOrders.entriesList().size, 0)
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertNull(orderBook.bestAskOrder)
    }

    @Test
    fun givenOrderBookWithBidOrders_whenGtcAskLimitOrderWithNotMatchPriceCreated_thenAddToQueue() {
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
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    3,
                    1,
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    OrderType.LIMIT_ORDER
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 2)
        Assertions.assertEquals(orderBook.askOrders.entriesList().size, 1)
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertEquals(orderBook.bestAskOrder, order)
    }

    @Test
    fun givenOrderBookWithBidAndAskOrders_whenGtcAskLimitOrderWithMatchPriceGreaterQuantityCreated_thenAddToQueue() {
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
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                3,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    1,
                    3,
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    OrderType.LIMIT_ORDER
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 0)
        Assertions.assertEquals(orderBook.askOrders.entriesList().size, 2)
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertEquals(orderBook.bestAskOrder, order)
    }

    @Test
    fun givenOrderBook_whenCancelBestBidOrder_thenBestBidOrderChange() {
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
        val lastOrder = orderBook.handleNewOrderCommand(
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
        //when
        orderBook.handleCancelCommand(OrderCancelCommand(firstOrderId, uuid, firstOrder!!.id()!!, pair))
        //then
        Assertions.assertEquals(orderBook.bestBidOrder, lastOrder)
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 1)
    }

    @Test
    fun givenOrderBookWithMoreBids_whenCancelBestBidOrder_thenBestBidOrderChange() {
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
        val secondOrder = orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                secondOrderId,
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
        //when
        orderBook.handleCancelCommand(OrderCancelCommand(firstOrderId, uuid, firstOrder!!.id()!!, pair))
        //then
        Assertions.assertEquals(orderBook.bestBidOrder, secondOrder)
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 2)
    }

    @Test
    fun givenOrderBookWithMoreBids_whenCancelABidOrder_thenBestBidOrderNotChange() {
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
        val secondOrder = orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                secondOrderId,
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
        //when
        orderBook.handleCancelCommand(OrderCancelCommand(secondOrderId, uuid, secondOrder!!.id()!!, pair))
        //then
        Assertions.assertEquals(orderBook.bestBidOrder, firstOrder)
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 2)
    }


    @Test
    fun givenOrderBookWithMoreBids_whenEditABidOrder_thenBestBidOrderChange() {
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
        //when
        val order = orderBook.handleEditCommand(
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
        Assertions.assertEquals(secondOrder.id(), order?.id())
        Assertions.assertEquals(orderBook.bestBidOrder, order)
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 3)
    }

    @Test
    fun givenOrderBookWithBidAndAskOrders_whenEditABidOrder_thenRefill() {
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
        val secondBid = orderBook.handleNewOrderCommand(
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
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                3,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        //when
        val order: SimpleOrder = orderBook.handleEditCommand(
            OrderEditCommand(
                UUID.randomUUID().toString(),
                uuid,
                secondBid!!.id()!!,
                pair,
                3,
                3
            )
        ) as SimpleOrder
        //then
        Assertions.assertEquals(2, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(0, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(orderBook.bestBidOrder, order)
        Assertions.assertNull(orderBook.bestAskOrder)
    }

    @Test
    fun givenEmptyOrderBook_whenGtcBidMarketOrderCreated_thenRejected() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        //when

        val order = orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                1,
                1,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.MARKET_ORDER
            )
        )
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 0)
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertNull(order)
    }

    @Test
    fun givenEmptyOrderBook_whenIocBidMarketOrderCreated_thenNoOrderCreated() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        //when

        val order = orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                1,
                1,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.MARKET_ORDER
            )
        )
        //then
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 0)
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertNull(order)
    }

    @Test
    fun givenOrderBookWithBidAndAskOrders_whenIocAskMarketOrderWithGreaterQuantityCreated_thenPartiallyFilled() {
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
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                3,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        val bestAskOrder = orderBook.bestAskOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    3,
                    OrderDirection.ASK,
                    MatchConstraint.IOC,
                    OrderType.MARKET_ORDER
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(2, order.filledQuantity)
        Assertions.assertEquals(orderBook.bidOrders.entriesList().size, 0)
        Assertions.assertEquals(orderBook.askOrders.entriesList().size, 1)
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertEquals(orderBook.bestAskOrder, bestAskOrder)
    }

    @Test
    fun givenOrderBookWithBidAndAskOrders_whenIocAskLimitOrderWithHigherPriceAndGreaterQuantityCreated_thenNotFilled() {
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
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                3,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        val bestAskOrder = orderBook.bestAskOrder
        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    3,
                    3,
                    OrderDirection.ASK,
                    MatchConstraint.IOC,
                    OrderType.LIMIT_ORDER
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(0, order.filledQuantity)
        Assertions.assertEquals(2, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(1, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(bestBidOrder, orderBook.bestBidOrder)
        Assertions.assertEquals(bestAskOrder, orderBook.bestAskOrder)
    }



    @Test
    fun whenSample1SequenceOfOrdersOccurs_thenAllSuccess() {

        val orderBook = SimpleOrderBook(ETH_BTC_PAIR, false)
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                ETH_BTC_PAIR,
                5000000,
                10000,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertEquals(1, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(1, orderBook.orders.size)

        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                ETH_BTC_PAIR,
                4900000,
                20000,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertNotNull(orderBook.bestAskOrder)
        Assertions.assertEquals(0, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(1, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(1, orderBook.orders.size)

        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                ETH_BTC_PAIR,
                4800000,
                10000,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        Assertions.assertNull(orderBook.bestBidOrder)
        Assertions.assertNotNull(orderBook.bestAskOrder)
        Assertions.assertEquals(0, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(2, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(2, orderBook.orders.size)

        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                ETH_BTC_PAIR,
                4850000,
                20000,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        Assertions.assertEquals(1, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(1, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(2, orderBook.orders.size)
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertNotNull(orderBook.bestAskOrder)

        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                ETH_BTC_PAIR,
                4850100,
                10000,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        Assertions.assertEquals(1, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(2, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(3, orderBook.orders.size)
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertNotNull(orderBook.bestAskOrder)

        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                ETH_BTC_PAIR,
                4849900,
                10000,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        ) as SimpleOrder
        Assertions.assertEquals(2, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(2, orderBook.askOrders.entriesList().size)
        Assertions.assertEquals(4, orderBook.orders.size)
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertNotNull(orderBook.bestAskOrder)
    }

    @Test
    fun givenOrderBookWithBidAndAskOrders_whenIocBudgetAskMarketOrderWithGreaterQuantityCreated_thenPartiallyFilled() {
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
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                1,
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
                3,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        val bestAskOrder = orderBook.bestAskOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    0,
                    OrderDirection.ASK,
                    MatchConstraint.IOC_BUDGET,
                    OrderType.MARKET_ORDER,
                    3
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(2, order.filledQuantity)
        Assertions.assertEquals(order.totalBudget, order.spentBudget)
        Assertions.assertEquals(1, orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(1, orderBook.askOrders.entriesList().size)
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertEquals(1, orderBook.bestBidOrder!!.filledQuantity)
        Assertions.assertEquals(bestAskOrder, orderBook.bestAskOrder)
    }

    @Test
    fun givenOrderBookWithBidAndAskOrders_whenIocBudgetBidMarketOrderWithGreaterQuantityCreated_thenPartiallyFilled() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                2,
                1,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
        )
        orderBook.handleNewOrderCommand(
            OrderCreateCommand(
                UUID.randomUUID().toString(),
                uuid,
                pair,
                3,
                1,
                OrderDirection.ASK,
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
        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    3,
                    OrderDirection.BID,
                    MatchConstraint.IOC_BUDGET,
                    OrderType.MARKET_ORDER,
                    3
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(1, order.filledQuantity)
        Assertions.assertEquals(2, order.spentBudget)
        Assertions.assertEquals(1,orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(1,orderBook.askOrders.entriesList().size)
        Assertions.assertNotNull(orderBook.bestAskOrder)
        Assertions.assertEquals(0, orderBook.bestAskOrder!!.filledQuantity)
        Assertions.assertEquals(orderBook.bestBidOrder, bestBidOrder)
    }

    /*
        Scenario 1: Buy IOC Market Order with Total Budget

    Initial Order Book (Asks):

        Sell Order A: Price = $10, Quantity = 100

        Sell Order B: Price = $12, Quantity = 50

        Sell Order C: Price = $15, Quantity = 200

    New Order:
    Buy IOC, Total Budget = $800

    Matching Process:

        Match with Sell Order A ($10):

            Cost for 100 shares: $10 × 100 = $1,000 (exceeds budget).

            Maximum affordable quantity: $800 / $10 = 80 shares.

            Cost: $10 × 80 = $800 (fully exhausts budget).

            Result: Partial fill of Sell Order A (80 shares). Remaining quantity in Sell Order A: 20 shares.

        Order is fully filled; no further matching (IOC cancels unfilled portion, but here it is fully filled).

    Order Book After:

        Sell Order A: Price = $10, Quantity = 20

        Sell Order B: Price = $12, Quantity = 50

        Sell Order C: Price = $15, Quantity = 200

         */

    @Test
    fun givenOrderBookWithScenario1_whenIocBudgetBidMarketOrderWithScenario1Received_thenOutputMatch() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        listOf(
            Pair(10L, 100L),
            Pair(12L, 50L),
            Pair(15L, 200L)
        ).forEach { orderData ->
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    orderData.first,
                    orderData.second,
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    OrderType.LIMIT_ORDER
                )
            )
        }

        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    0,
                    OrderDirection.BID,
                    MatchConstraint.IOC_BUDGET,
                    OrderType.MARKET_ORDER,
                    800
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(80, order.filledQuantity)
        Assertions.assertEquals(800, order.spentBudget)
        Assertions.assertEquals(0,orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(3,orderBook.askOrders.entriesList().size)
        Assertions.assertNotNull(orderBook.bestAskOrder)
        Assertions.assertEquals(80, orderBook.bestAskOrder!!.filledQuantity)
        Assertions.assertEquals(orderBook.bestBidOrder, bestBidOrder)
    }

    /*
    Scenario 2: Buy IOC Market Order with Insufficient Liquidity

    Initial Order Book (Asks):

        Sell Order A: Price = $20, Quantity = 30

    New Order:
    Buy IOC, Total Budget = $1,000

    Matching Process:

        Match with Sell Order A ($20):

            Cost for 30 shares: $20 × 30 = $600 (within budget).

            Remaining budget: $400.

            Result: Fully fill Sell Order A.

        No more asks; the remaining budget $400 is canceled (IOC rule).

    Order Book After:
    Empty (all asks consumed).
     */
    @Test
    fun givenOrderBookWithScenario2_whenIocBudgetBidMarketOrderWithScenario2Received_thenOutputMatch() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        listOf(
            Pair(20L, 30L)
        ).forEach { orderData ->
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    orderData.first,
                    orderData.second,
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    OrderType.LIMIT_ORDER
                )
            )
        }

        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    0,
                    OrderDirection.BID,
                    MatchConstraint.IOC_BUDGET,
                    OrderType.MARKET_ORDER,
                    1000
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(30, order.filledQuantity)
        Assertions.assertEquals(600, order.spentBudget)
        Assertions.assertEquals(0,orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(0,orderBook.askOrders.entriesList().size)
        Assertions.assertNull(orderBook.bestAskOrder)
        Assertions.assertEquals(orderBook.bestBidOrder, bestBidOrder)
    }

    /*
    Scenario 3: Sell IOC Market Order with Total Budget (Minimum Proceeds)

    Initial Order Book (Bids):

        Buy Order X: Price = $18, Quantity = 100

        Buy Order Y: Price = $16, Quantity = 150

        Buy Order Z: Price = $14, Quantity = 200

    New Order:
    Sell IOC, Total Budget (Minimum Proceeds) = $2,008

    Matching Process:

        Match with Buy Order X ($18):

            Proceeds from 100 shares: $18 × 100 = $1,800.

            Remaining to meet budget: $200.

            Result: Fully fill Buy Order X.

        Match with Buy Order Y ($16):

            Required additional proceeds: $208.

            Quantity needed: $200 / $16 = 13 shares.

            Proceeds: $16 × 13 = $208.

            Result: Partial fill of Buy Order Y (13 shares). Remaining quantity in Buy Order Y: 137 shares.

        Total proceeds = $1,800 + $192 = $2,008 (budget met).

    Order Book After:

        Buy Order Y: Price = $16, Quantity = 137

        Buy Order Z: Price = $14, Quantity = 200
     */

    @Test
    fun givenOrderBookWithScenario3_whenIocBudgetAskMarketOrderWithScenario3Received_thenOutputMatch() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        listOf(
            Pair(18L, 100L),
            Pair(16L, 150L),
            Pair(14L, 200L),
        ).forEach { orderData ->
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    orderData.first,
                    orderData.second,
                    OrderDirection.BID,
                    MatchConstraint.GTC,
                    OrderType.LIMIT_ORDER
                )
            )
        }

        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    0,
                    OrderDirection.ASK,
                    MatchConstraint.IOC_BUDGET,
                    OrderType.MARKET_ORDER,
                    2000
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(113, order.filledQuantity)
        Assertions.assertEquals(2008, order.spentBudget)
        Assertions.assertEquals(2,orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(0,orderBook.askOrders.entriesList().size)
        Assertions.assertNull(orderBook.bestAskOrder)
        Assertions.assertEquals(13, orderBook.bestBidOrder!!.filledQuantity)
        Assertions.assertNotEquals(orderBook.bestBidOrder, bestBidOrder)
    }

    /*

Scenario 4: Sell IOC Market Order with Budget Not Met

    Initial Order Book (Bids):

        Buy Order X: Price = $5, Quantity = 100

    New Order:
    Sell IOC, Total Budget (Minimum Proceeds) = $1,000

    Matching Process:

        Match with Buy Order X ($5):

            Maximum proceeds: $5 × 100 = $500 (less than $1,000).

            Result: Order canceled entirely (IOC rule: no partial execution if budget is unmet).

    Order Book After:
    Unchanged (Buy Order X remains)
     */
    @Test
    fun givenOrderBookWithScenario4_whenIocBudgetAskMarketOrderWithScenario4Received_thenOutputMatch() {
        //given
        val orderBook = SimpleOrderBook(pair, false)
        listOf(
            Pair(5L, 100L)
        ).forEach { orderData ->
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    orderData.first,
                    orderData.second,
                    OrderDirection.BID,
                    MatchConstraint.GTC,
                    OrderType.LIMIT_ORDER
                )
            )
        }

        val bestBidOrder = orderBook.bestBidOrder
        //when
        val order: SimpleOrder =
            orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    UUID.randomUUID().toString(),
                    uuid,
                    pair,
                    0,
                    0,
                    OrderDirection.ASK,
                    MatchConstraint.IOC_BUDGET,
                    OrderType.MARKET_ORDER,
                    1000
                )
            ) as SimpleOrder
        //then
        Assertions.assertEquals(0, order.filledQuantity)
        Assertions.assertEquals(0, order.spentBudget)
        Assertions.assertEquals(1,orderBook.bidOrders.entriesList().size)
        Assertions.assertEquals(0,orderBook.askOrders.entriesList().size)
        Assertions.assertNull(orderBook.bestAskOrder)
        Assertions.assertNotNull(orderBook.bestBidOrder)
        Assertions.assertEquals(0, orderBook.bestBidOrder!!.filledQuantity)

    }
}