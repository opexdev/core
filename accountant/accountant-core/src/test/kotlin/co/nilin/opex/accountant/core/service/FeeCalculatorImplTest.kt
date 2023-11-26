package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FeeCalculatorImplTest {

    private val receiverAddress = "0x0"
    private val feeCalculator = FeeCalculatorImpl(receiverAddress, JsonMapperTestImpl())

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_feeActionsNotNull(): Unit = runBlocking {
        val actions =
            feeCalculator.createFeeActions(Valid.tradeEvent, Valid.makerOrder, Valid.takerOrder, null, null)
        assertThat(actions.makerFeeAction).isNotNull
        assertThat(actions.makerFeeAction.detail["ouid"]).isEqualTo(Valid.makerOrder.ouid)
        assertThat(actions.takerFeeAction).isNotNull
        assertThat(actions.takerFeeAction.detail["ouid"]).isEqualTo(Valid.takerOrder.ouid)
    }

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_returnCorrectFees(): Unit = runBlocking {
        val actions =
            feeCalculator.createFeeActions(Valid.tradeEvent, Valid.makerOrder, Valid.takerOrder, null, null)
        with(actions.makerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(0.01) // 1% of 1 BTC
            assertThat(symbol).isEqualTo("BTC")
            assertThat(sender).isEqualTo("user_1")
            assertThat(pointer).isEqualTo("order_2")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo("exchange")
            assertThat(detail["ouid"]).isEqualTo(Valid.makerOrder.ouid)
        }

        with(actions.takerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(500.0) // 1% of 50,000 USDT
            assertThat(symbol).isEqualTo("USDT")
            assertThat(sender).isEqualTo("user_2")
            assertThat(pointer).isEqualTo("order_1")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo("exchange")
            assertThat(detail["ouid"]).isEqualTo(Valid.takerOrder.ouid)
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
            Valid.currentTime,
            FinancialActionCategory.TRADE,
            emptyMap()
        )

        val actions = feeCalculator.createFeeActions(
            Valid.tradeEvent,
            Valid.makerOrder,
            Valid.takerOrder,
            parentFA,
            parentFA
        )
        assertThat(actions.makerFeeAction.parent).isNotNull
        assertThat(actions.takerFeeAction.parent).isNotNull
    }

}