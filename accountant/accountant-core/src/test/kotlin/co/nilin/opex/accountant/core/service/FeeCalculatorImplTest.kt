package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.accountant.core.model.UserFee
import co.nilin.opex.accountant.core.model.WalletType
import co.nilin.opex.accountant.core.spi.FeeConfigService
import co.nilin.opex.accountant.core.spi.UserVolumePersister
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.core.utils.CacheManager
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FeeCalculatorImplTest {
    private val walletProxy = mockk<WalletProxy>()
    private val feeConfigService = mockk<FeeConfigService>()
    private val userVolumePersister = mockk<UserVolumePersister>()
    private val cacheManager = mockk<CacheManager<String, UserFee>>()

    private val receiverAddress = "0x0"
    private val feeCalculator = FeeCalculatorImpl(
        walletProxy,
        feeConfigService,
        userVolumePersister,
        cacheManager,
        receiverAddress,
        "GMT+03:30",
        JsonMapperTestImpl(),
    )

    @Test
    fun givenTradeEventsAndOrders_whenFeeCalculated_feeActionsNotNull(): Unit = runBlocking {
        val actions =
            feeCalculator.createFeeActions(Valid.tradeEvent, Valid.makerOrder, Valid.takerOrder, null, null)
        assertThat(actions.makerFeeAction).isNotNull
        assertThat(actions.takerFeeAction).isNotNull
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
            assertThat(receiverWalletType).isEqualTo(WalletType.EXCHANGE)
        }

        with(actions.takerFeeAction) {
            assertThat(amount.toDouble()).isEqualTo(500.0) // 1% of 50,000 USDT
            assertThat(symbol).isEqualTo("USDT")
            assertThat(sender).isEqualTo("user_2")
            assertThat(pointer).isEqualTo("order_1")
            assertThat(receiver).isEqualTo(receiverAddress)
            assertThat(receiverWalletType).isEqualTo(WalletType.EXCHANGE)
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
            WalletType.MAIN,
            "system",
            WalletType.MAIN,
            Valid.currentTime,
            FinancialActionCategory.TRADE
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