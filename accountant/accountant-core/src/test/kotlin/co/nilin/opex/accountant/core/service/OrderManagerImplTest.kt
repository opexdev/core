package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import java.math.BigDecimal
import java.time.LocalDateTime

internal class OrderManagerImplTest {

    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val financialActionLoader = mockk<FinancialActionLoader>()
    private val orderPersister = mockk<OrderPersister>()
    private val tempEventPersister = mockk<TempEventPersister>()
    private val pairConfigLoader = mockk<PairConfigLoader>()
    private val richOrderPublisher = mockk<RichOrderPublisher>()
    private val orderManager = OrderManagerImpl(
        pairConfigLoader,
        financialActionPersister,
        financialActionLoader,
        orderPersister,
        tempEventPersister,
        richOrderPublisher
    )

    init {
        coEvery { tempEventPersister.loadTempEvents(any()) } returns emptyList()
        coEvery { orderPersister.save(any()) } returns any()
    }

    @Test
    fun givenAskOrder_whenHandleRequestOrder_thenFAMatch(): Unit = runBlocking {
        //given
        val pair = co.nilin.opex.matching.engine.core.model.Pair("ETH", "BTC")
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
            pairConfigLoader.load(pair.toString(), submitOrderEvent.direction, "")
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
            LocalDateTime.now()
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
        val pair = co.nilin.opex.matching.engine.core.model.Pair("eth", "btc")
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
            pairConfigLoader.load(pair.toString(), submitOrderEvent.direction, "")
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
            LocalDateTime.now()
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
    fun handleNewOrder() {
    }

    @Test
    fun handleUpdateOrder() {
    }

    @Test
    fun handleRejectOrder() {
    }

    @Test
    fun handleCancelOrder() {
    }
}


