package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.matching.engine.core.eventh.events.CancelOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.CreateOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.RejectOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.engine.core.inout.RejectReason
import co.nilin.opex.matching.engine.core.inout.RequestedOperation
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import java.math.BigDecimal

internal class OrderManagerImplTest {

    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val financialActionLoader = mockk<FinancialActionLoader>()
    private val orderPersister = mockk<OrderPersister>()
    private val tempEventPersister = mockk<TempEventPersister>()
    private val pairConfigLoader = mockk<PairConfigLoader>()
    private val richOrderPublisher = mockk<RichOrderPublisher>()
    private val userLevelLoader = mockk<UserLevelLoader>()

    private val orderManager = OrderManagerImpl(
        pairConfigLoader,
        userLevelLoader,
        financialActionPersister,
        financialActionLoader,
        orderPersister,
        tempEventPersister,
        richOrderPublisher
    )

    init {
        coEvery { tempEventPersister.loadTempEvents(any()) } returns emptyList()
        coEvery { orderPersister.save(any()) } returnsArgument (0)
        coEvery { richOrderPublisher.publish(any()) } returnsArgument (0)
        coEvery { tempEventPersister.saveTempEvent(any(), any()) } returns any()
        coEvery { financialActionLoader.findLast(any(), any()) } returns null
        coEvery { financialActionPersister.persist(any()) } returnsArgument (0)
        coEvery { userLevelLoader.load(any()) } returns "*"
    }

    @Test
    fun givenAskOrder_whenHandleRequestOrder_thenFAMatch(): Unit = runBlocking {
        //given
        val pair = Pair("ETH", "BTC")
        val pairConfig = PairConfig(
            pair.toString(),
            pair.leftSideName,
            pair.rightSideName,
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(0.001)
        )
        val submitOrderEvent = SubmitOrderEvent(
            "ouid", "uuid", null, pair, 30, 60, 0, OrderDirection.ASK, MatchConstraint.GTC, OrderType.LIMIT_ORDER
        )

        coEvery {
            pairConfigLoader.load(pair.toString(), submitOrderEvent.direction, any())
        } returns PairFeeConfig(
            pairConfig,
            submitOrderEvent.direction.toString(),
            "",
            BigDecimal.valueOf(0.1),
            BigDecimal.valueOf(0.12)
        )

        coEvery { financialActionPersister.persist(any()) } returnsArgument (0)

        //when
        val financialActions = orderManager.handleRequestOrder(submitOrderEvent)

        //then
        assertThat(financialActions.size).isEqualTo(1)
        val expectedFinancialAction = FinancialAction(
            null,
            SubmitOrderEvent::class.simpleName!!,
            submitOrderEvent.ouid,
            pair.leftSideName,
            pairConfig.leftSideFraction.multiply(submitOrderEvent.quantity.toBigDecimal()),
            submitOrderEvent.uuid,
            "main",
            submitOrderEvent.uuid,
            "exchange",
            Valid.currentTime
        )

        with(expectedFinancialAction) {
            assertThat(eventType).isEqualTo(financialActions[0].eventType)
            assertThat(symbol).isEqualTo(financialActions[0].symbol)
            assertThat(amount).isEqualTo(financialActions[0].amount)
            assertThat(sender).isEqualTo(financialActions[0].sender)
            assertThat(senderWalletType).isEqualTo(financialActions[0].senderWalletType)
            assertThat(receiver).isEqualTo(financialActions[0].receiver)
            assertThat(receiverWalletType).isEqualTo(financialActions[0].receiverWalletType)
        }
    }

    @Test
    fun givenBidOrder_whenHandleRequestOrder_thenFAMatch(): Unit = runBlocking {
        //given
        val pair = Pair("eth", "btc")
        val pairConfig = PairConfig(
            pair.toString(),
            pair.leftSideName,
            pair.rightSideName,
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(0.001)
        )
        val submitOrderEvent = SubmitOrderEvent(
            "ouid", "uuid", null, pair, 35, 14, 0, OrderDirection.BID, MatchConstraint.GTC, OrderType.LIMIT_ORDER
        )

        coEvery {
            pairConfigLoader.load(pair.toString(), submitOrderEvent.direction, any())
        } returns PairFeeConfig(
            pairConfig,
            submitOrderEvent.direction.toString(),
            "",
            BigDecimal.valueOf(0.08),
            BigDecimal.valueOf(0.1)
        )

        coEvery { financialActionPersister.persist(any()) } returnsArgument (0)

        //when
        val financialActions = orderManager.handleRequestOrder(submitOrderEvent)

        //then
        assertThat(financialActions.size).isEqualTo(1)
        val expectedFinancialAction = FinancialAction(
            null,
            SubmitOrderEvent::class.simpleName!!,
            submitOrderEvent.ouid,
            pair.rightSideName,
            pairConfig.leftSideFraction.multiply(submitOrderEvent.quantity.toBigDecimal())
                .multiply(pairConfig.rightSideFraction)
                .multiply(submitOrderEvent.price.toBigDecimal()),
            submitOrderEvent.uuid,
            "main",
            submitOrderEvent.uuid,
            "exchange",
            Valid.currentTime
        )
        with(expectedFinancialAction) {
            assertThat(eventType).isEqualTo(financialActions[0].eventType)
            assertThat(symbol).isEqualTo(financialActions[0].symbol)
            assertThat(amount).isEqualTo(financialActions[0].amount)
            assertThat(sender).isEqualTo(financialActions[0].sender)
            assertThat(senderWalletType).isEqualTo(financialActions[0].senderWalletType)
            assertThat(receiver).isEqualTo(financialActions[0].receiver)
            assertThat(receiverWalletType).isEqualTo(financialActions[0].receiverWalletType)
        }
    }

    @Test
    fun givenNewOrderEventReceived_whenUpdatingOrder_matchingEngineIdMatch(): Unit = runBlocking {
        val orderEvent = CreateOrderEvent(
            "order_ouid",
            "user_1",
            55,
            Pair("BTC", "USDT"),
            100000,
            1000,
            0,
            OrderDirection.BID
        )

        coEvery { orderPersister.load(any()) } returns Valid.order

        val fa = orderManager.handleNewOrder(orderEvent)

        assertThat(fa.size).isEqualTo(0)
        assertThat(Valid.order.matchingEngineId).isEqualTo(55)
        coVerify(exactly = 1) { richOrderPublisher.publish(any()) }
    }

    @Test
    fun givenNewOrderEventReceived_whenLocalOrderNull_saveTempEvent(): Unit = runBlocking {
        val orderEvent = CreateOrderEvent(
            "order_ouid",
            "user_1",
            55,
            Pair("BTC", "USDT"),
            100000,
            1000,
            0,
            OrderDirection.BID
        )

        coEvery { orderPersister.load(any()) } returns null

        val fa = orderManager.handleNewOrder(orderEvent)

        assertThat(fa.size).isEqualTo(0)
        coVerify(exactly = 1) { tempEventPersister.saveTempEvent(any(), any()) }
    }

    @Test
    fun givenRejectOrderReceived_whenLocalOrderNull_saveTempEvent(): Unit = runBlocking {
        val orderEvent = RejectOrderEvent(
            "ouid",
            "user_1",
            56,
            Pair("BTC", "USDT"),
            RequestedOperation.CANCEL_ORDER,
            RejectReason.ORDER_NOT_FOUND
        )

        coEvery { orderPersister.load(any()) } returns null

        val fa = orderManager.handleRejectOrder(orderEvent)

        assertThat(fa.size).isEqualTo(0)
        coVerify(exactly = 1) { tempEventPersister.saveTempEvent(any(), any()) }
    }

    @Test
    fun givenRejectOrderReceived_whenLocalFound_publishRichOrderUpdate(): Unit = runBlocking {
        val orderEvent = RejectOrderEvent(
            "ouid",
            "user_1",
            56,
            Pair("BTC", "USDT"),
            100000,
            1000,
            OrderDirection.BID,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER,
            RequestedOperation.CANCEL_ORDER,
            RejectReason.ORDER_NOT_FOUND,
        )
        coEvery { orderPersister.load(any()) } returns Valid.order

        val fa = orderManager.handleRejectOrder(orderEvent)[0]

        assertThat(fa.amount).isEqualTo(Valid.order.remainedTransferAmount)
        assertThat(fa.symbol).isEqualTo(orderEvent.pair.rightSideName)
        assertThat(Valid.order.status).isEqualTo(OrderStatus.REJECTED.code)

        coVerify(exactly = 1) { richOrderPublisher.publish(any()) }
        coVerify(exactly = 1) { orderPersister.save(any()) }
    }

    @Test
    fun givenCancelOrderReceived_whenLocalOrderNull_saveTempEvent(): Unit = runBlocking {
        val orderEvent = CancelOrderEvent(
            "order_ouid",
            "user_id",
            88,
            Pair("BTC", "USDT"),
            100000,
            1000,
            500,
            OrderDirection.BID
        )

        coEvery { orderPersister.load(any()) } returns null

        val fa = orderManager.handleCancelOrder(orderEvent)

        assertThat(fa.size).isEqualTo(0)
        coVerify(exactly = 1) { tempEventPersister.saveTempEvent(any(), any()) }
    }

    @Test
    fun givenCancelOrderReceived_whenLocalFound_publishRichOrderUpdate(): Unit = runBlocking {
        val orderEvent = CancelOrderEvent(
            "order_ouid",
            "user_id",
            88,
            Pair("BTC", "USDT"),
            100000,
            1000,
            500,
            OrderDirection.BID
        )
        coEvery { orderPersister.load(any()) } returns Valid.order

        val fa = orderManager.handleCancelOrder(orderEvent)[0]

        assertThat(fa.amount).isEqualTo(Valid.order.remainedTransferAmount)
        assertThat(fa.symbol).isEqualTo(orderEvent.pair.rightSideName)
        assertThat(Valid.order.status).isEqualTo(OrderStatus.CANCELED.code)

        coVerify(exactly = 1) { richOrderPublisher.publish(any()) }
        coVerify(exactly = 1) { orderPersister.save(any()) }
    }

    //TODO
    @Test
    fun handleUpdateOrder() {
    }

}


