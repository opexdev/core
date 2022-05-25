package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

internal class FeeCalculatorImplTest {

    private val receiverAddress = "0x0"
    private val feeCalculator = FeeCalculatorImpl(receiverAddress)
    private var defaultMaker: Order = Order(
        "BTC_USDT",
        "order_1",
        1,
        0.01.toBigDecimal(),
        0.01.toBigDecimal(),
        0.000001.toBigDecimal(),
        0.01.toBigDecimal(),
        "user_1",
        "*",
        OrderDirection.BID,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        50_000.toBigDecimal().divide(0.01.toBigDecimal()).longValueExact(),
        1.toBigDecimal().divide(0.000001.toBigDecimal()).longValueExact(),
        1.toBigDecimal().divide(0.000001.toBigDecimal()).longValueExact(),
        50_000.toBigDecimal(),
        1.toBigDecimal(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        OrderStatus.FILLED.code
    )
    private var defaultTaker: Order = Order(
        "BTC_USDT",
        "order_2",
        2,
        0.01.toBigDecimal(),
        0.01.toBigDecimal(),
        0.000001.toBigDecimal(),
        0.01.toBigDecimal(),
        "user_2",
        "*",
        OrderDirection.ASK,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        50_000.toBigDecimal().divide(0.01.toBigDecimal()).longValueExact(),
        1.toBigDecimal().divide(0.000001.toBigDecimal()).longValueExact(),
        1.toBigDecimal().divide(0.000001.toBigDecimal()).longValueExact(),
        50_000.toBigDecimal(),
        1.toBigDecimal(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        OrderStatus.FILLED.code
    )
    private var defaultTrade: TradeEvent = TradeEvent(
        1,
        Pair("BTC", "USDT"),
        "order_2",
        "user_2",
        2,
        OrderDirection.ASK,
        (50_000 / 0.01).toLong(),
        0,
        "order_1",
        "user_1",
        1,
        OrderDirection.BID,
        (50_000 / 0.01).toLong(),
        0,
        (1 / 0.000001).toLong()
    )

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_feeActionsNotNull(): Unit = runBlocking {
        val actions = feeCalculator.createFeeActions(defaultTrade, defaultMaker, defaultTaker, null, null)
        assertThat(actions.makerFeeAction).isNotNull
        assertThat(actions.takerFeeAction).isNotNull
    }

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_returnCorrectFees(): Unit = runBlocking {
        val actions = feeCalculator.createFeeActions(defaultTrade, defaultMaker, defaultTaker, null, null)
        with(actions.makerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(0.01) // 1% of 1 BTC
            assertThat(symbol).isEqualTo("BTC")
            assertThat(sender).isEqualTo("user_1")
            assertThat(pointer).isEqualTo("order_2")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo("exchange")
        }

        with(actions.takerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(500.0) // 1% of 50,000 USDT
            assertThat(symbol).isEqualTo("USDT")
            assertThat(sender).isEqualTo("user_2")
            assertThat(pointer).isEqualTo("order_1")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo("exchange")
        }
    }

    @Test
    fun givenTradeEventsAndOrders_whenFAParentNotNull_thenFeeActionParentNotNull(): Unit = runBlocking {
        val parentFA = FinancialAction(
            null,
            TradeEvent::class.java.name,
            "trade_id",
            "BTC_USDT",
            10000.0.toBigDecimal(),
            "user_parent",
            "main",
            "system",
            "main",
            LocalDateTime.now()
        )

        val actions = feeCalculator.createFeeActions(defaultTrade, defaultMaker, defaultTaker, parentFA, parentFA)
        assertThat(actions.makerFeeAction.parent).isNotNull
        assertThat(actions.takerFeeAction.parent).isNotNull
    }

}