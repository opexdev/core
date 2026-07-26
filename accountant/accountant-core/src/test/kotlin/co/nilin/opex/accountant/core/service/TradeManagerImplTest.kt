package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FeeCalculator
import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.common.utils.CacheManager
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class TradeManagerImplTest {

    private val financialActionPersister = mockk<FinancialActionPersister>()
    private val financeActionLoader = mockk<FinancialActionLoader>()
    private val orderPersister = mockk<OrderPersister>()
    private val pairConfigLoader = mockk<PairConfigLoader>()
    private val tempEventPersister = mockk<TempEventPersister>()
    private val richOrderPublisher = mockk<RichOrderPublisher>()
    private val richTradePublisher = mockk<RichTradePublisher>()
    private val financialActionPublisher = mockk<FinancialActionPublisher>()
    private val currencyRatePersister = mockk<CurrencyRatePersister>()
    private val userVolumePersister = mockk<UserTradeVolumePersister>()
    private val feeCalculator = mockk<FeeCalculator>()
    private val walletProxy = mockk<WalletProxy>()
    private val feeConfigService = mockk<FeeConfigService>()
    private val cacheManager = mockk<CacheManager<String, UserFee>>()

    private val jsonMapper = JsonMapperTestImpl()

    private val orderManager = OrderManagerImpl(
        pairConfigLoader,
        financialActionPersister,
        financeActionLoader,
        orderPersister,
        tempEventPersister,
        richOrderPublisher,
        financialActionPublisher,
        feeCalculator
    )

    private val tradeManager = TradeManagerImpl(
        financialActionPersister,
        financeActionLoader,
        orderPersister,
        tempEventPersister,
        richTradePublisher,
        richOrderPublisher,
        FeeCalculatorImpl(
            walletProxy,
            feeConfigService,
            userVolumePersister,
            cacheManager,
            "0x0",
            "+03:30",
            "USDT",
            JsonMapperTestImpl()
        ),
        financialActionPublisher,
        currencyRatePersister,
        userVolumePersister,
        "USDT",
        "+03:30",
    )

    init {
        coEvery { tempEventPersister.loadTempEvents(any()) } returns emptyList()
        coEvery { orderPersister.save(any()) } returnsArgument (0)
        coEvery { financeActionLoader.findLast(any(), any()) } returns null
        coEvery { richOrderPublisher.publish(any()) } returns Unit
        coEvery { richTradePublisher.publish(any()) } returns Unit
        coEvery { financialActionPublisher.publish(any()) } returns Unit
        coEvery { financialActionPersister.updateStatus(any<FinancialAction>(), any()) } returns Unit
        coEvery { financialActionPersister.updateStatus(any<String>(), any()) } returns Unit
        coEvery { currencyRatePersister.updateRate(any(), any(), any()) } just runs
        coEvery { userVolumePersister.update(any(), any(), any(), any(), any(), any()) } just runs
        coEvery { currencyRatePersister.getRate(any(), any()) } returns BigDecimal.ONE
    }

    @Test
    fun givenSellOrder_WhenMatchBuyOrderCome_thenFAMatched(): Unit = runBlocking {
        //given
        val pair = Pair("eth", "btc")
        val pairConfig = PairConfig(
            pair.toString(),
            pair.leftSideName,
            pair.rightSideName,
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(0.01)
        )
        val makerSubmitOrderEvent = SubmitOrderEvent(
            "mouid",
            "muuid",
            null,
            pair,
            60000,
            2,
            2,
            OrderDirection.ASK,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )
        prepareOrder(pair, pairConfig, makerSubmitOrderEvent, BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.12))

        val takerSubmitOrderEvent = SubmitOrderEvent(
            "touid",
            "tuuid",
            null,
            pair,
            70000,
            2,
            2,
            OrderDirection.BID,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )

        prepareOrder(pair, pairConfig, takerSubmitOrderEvent, BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.1))

        val tradeEvent = makeTradeEvent(pair, takerSubmitOrderEvent, makerSubmitOrderEvent, 1)
        //when
        val tradeFinancialActions = tradeManager.handleTrade(tradeEvent)

        assertThat(tradeFinancialActions.size).isEqualTo(4)
        assertThat(tradeFinancialActions[0].category).isEqualTo(FinancialActionCategory.TRADE)
        assertThat(tradeFinancialActions[1].category).isEqualTo(FinancialActionCategory.TRADE)
        assertThat(tradeFinancialActions[2].category).isEqualTo(FinancialActionCategory.FEE)
        assertThat(tradeFinancialActions[3].category).isEqualTo(FinancialActionCategory.FEE)

        assertThat((makerSubmitOrderEvent.price.toBigDecimal() * pairConfig.rightSideFraction).stripTrailingZeros())
            .isEqualTo(tradeFinancialActions[0].amount.stripTrailingZeros())
    }

    @Test
    fun givenBuyOrder_whenMatchSellOrderCome_thenFAMatched(): Unit = runBlocking {
        //given
        val pair = Pair("eth", "btc")
        val pairConfig = PairConfig(
            pair.toString(),
            pair.leftSideName,
            pair.rightSideName,
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(0.001)
        )
        val makerSubmitOrderEvent = SubmitOrderEvent(
            "mouid",
            "muuid",
            null,
            pair,
            70000,
            2,
            2,
            OrderDirection.BID,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )
        prepareOrder(pair, pairConfig, makerSubmitOrderEvent, BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.12))

        val takerSubmitOrderEvent = SubmitOrderEvent(
            "touid",
            "tuuid",
            null,
            pair,
            60000,
            2,
            2,
            OrderDirection.ASK,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )

        prepareOrder(pair, pairConfig, takerSubmitOrderEvent, BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.1))

        val tradeEvent = makeTradeEvent(pair, takerSubmitOrderEvent, makerSubmitOrderEvent, 1)
        //when
        val tradeFinancialActions = tradeManager.handleTrade(tradeEvent)

        assertThat(tradeFinancialActions.size).isEqualTo(4)
        assertThat((makerSubmitOrderEvent.price.toBigDecimal() * pairConfig.rightSideFraction).stripTrailingZeros())
            .isEqualTo(tradeFinancialActions[1].amount.stripTrailingZeros())
    }

    @Test
    fun givenSellOrderWith1Remains_whenMatchBuyOrderCome_thenFAMatched(): Unit = runBlocking {
        //given
        val pair = Pair("btc", "eth")
        val pairConfig = PairConfig(
            pair.toString(),
            pair.leftSideName,
            pair.rightSideName,
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(0.01)
        )
        val makerSubmitOrderEvent = SubmitOrderEvent(
            "mouid",
            "muuid",
            null,
            pair,
            60000,
            1,
            1,
            OrderDirection.ASK,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )
        prepareOrder(pair, pairConfig, makerSubmitOrderEvent, BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.12))

        val takerSubmitOrderEvent = SubmitOrderEvent(
            "touid",
            "tuuid",
            null,
            pair,
            70000,
            2,
            2,
            OrderDirection.BID,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )

        prepareOrder(pair, pairConfig, takerSubmitOrderEvent, BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.1))

        val tradeEvent = makeTradeEvent(pair, takerSubmitOrderEvent, makerSubmitOrderEvent, 1)
        //when
        val tradeFinancialActions = tradeManager.handleTrade(tradeEvent)

        assertThat(tradeFinancialActions.size).isEqualTo(4)
        assertThat(tradeFinancialActions[0].category).isEqualTo(FinancialActionCategory.TRADE)
        assertThat(tradeFinancialActions[1].category).isEqualTo(FinancialActionCategory.TRADE)
        assertThat(tradeFinancialActions[2].category).isEqualTo(FinancialActionCategory.FEE)
        assertThat(tradeFinancialActions[3].category).isEqualTo(FinancialActionCategory.FEE)

        assertThat((makerSubmitOrderEvent.price.toBigDecimal() * pairConfig.rightSideFraction).stripTrailingZeros())
            .isEqualTo(tradeFinancialActions[0].amount.stripTrailingZeros())
    }

    @Test
    fun givenSellOrder_whenMatchBuyOrderWith1RemainsCome_thenFAMatched(): Unit = runBlocking {
        //given
        val pair = Pair("btc", "eth")
        val pairConfig = PairConfig(
            pair.toString(),
            pair.leftSideName,
            pair.rightSideName,
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(0.01)
        )
        val makerSubmitOrderEvent = SubmitOrderEvent(
            "mouid",
            "muuid",
            null,
            pair,
            60000,
            2,
            2,
            OrderDirection.ASK,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )
        prepareOrder(pair, pairConfig, makerSubmitOrderEvent, BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.12))

        val takerSubmitOrderEvent = SubmitOrderEvent(
            "touid",
            "tuuid",
            null,
            pair,
            70000,
            1,
            1,
            OrderDirection.BID,
            MatchConstraint.GTC,
            OrderType.LIMIT_ORDER
        )

        prepareOrder(pair, pairConfig, takerSubmitOrderEvent, BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.1))

        val tradeEvent = makeTradeEvent(pair, takerSubmitOrderEvent, makerSubmitOrderEvent, 1)
        //when
        val tradeFinancialActions = tradeManager.handleTrade(tradeEvent)

        assertThat(tradeFinancialActions.size).isEqualTo(5)
        assertThat(tradeFinancialActions[0].category).isEqualTo(FinancialActionCategory.TRADE)
        assertThat(tradeFinancialActions[1].category).isEqualTo(FinancialActionCategory.ORDER_FINALIZED)
        assertThat(tradeFinancialActions[2].category).isEqualTo(FinancialActionCategory.TRADE)
        assertThat(tradeFinancialActions[3].category).isEqualTo(FinancialActionCategory.FEE)
        assertThat(tradeFinancialActions[4].category).isEqualTo(FinancialActionCategory.FEE)

        assertThat((makerSubmitOrderEvent.price.toBigDecimal() * pairConfig.rightSideFraction).stripTrailingZeros())
            .isEqualTo(tradeFinancialActions[0].amount.stripTrailingZeros())
    }

    private fun makeTradeEvent(
        pair: Pair,
        takerSubmitOrderEvent: SubmitOrderEvent,
        makerSubmitOrderEvent: SubmitOrderEvent,
        matchedQuantity: Long
    ): TradeEvent {
        return TradeEvent(
            0,
            pair,
            takerSubmitOrderEvent.ouid,
            takerSubmitOrderEvent.uuid,
            takerSubmitOrderEvent.orderId ?: -1,
            takerSubmitOrderEvent.direction,
            takerSubmitOrderEvent.price,
            takerSubmitOrderEvent.remainedQuantity,
            makerSubmitOrderEvent.ouid,
            makerSubmitOrderEvent.uuid,
            makerSubmitOrderEvent.orderId ?: 1,
            makerSubmitOrderEvent.direction,
            makerSubmitOrderEvent.price,
            makerSubmitOrderEvent.remainedQuantity,
            matchedQuantity
        )
    }

    private suspend fun prepareOrder(
        pair: Pair,
        pairConfig: PairConfig,
        submitOrderEvent: SubmitOrderEvent,
        makerFee: BigDecimal,
        takerFee: BigDecimal
    ) {
        coEvery {
            feeCalculator.getUserFee(submitOrderEvent.uuid)
        } returns UserFee(
            "Test", makerFee, takerFee
        )

        coEvery {
            pairConfigLoader.load(pair.toString(), submitOrderEvent.direction)
        } returns pairConfig

        coEvery { financialActionPersister.persist(any()) } returnsArgument (0)

        val financialActions = orderManager.handleRequestOrder(submitOrderEvent)

        val pairConfig =
            pairConfigLoader.load(submitOrderEvent.pair.toString(), submitOrderEvent.direction)
        val orderMakerFee = makerFee
        val orderTakerFee = takerFee

        coEvery { orderPersister.load(submitOrderEvent.ouid) } returns Order(
            submitOrderEvent.pair.toString(),
            submitOrderEvent.ouid,
            null,
            orderMakerFee,
            orderTakerFee,
            pairConfig.leftSideFraction,
            pairConfig.rightSideFraction,
            submitOrderEvent.uuid,
            submitOrderEvent.userLevel,
            submitOrderEvent.direction,
            submitOrderEvent.matchConstraint,
            submitOrderEvent.orderType,
            submitOrderEvent.price,
            submitOrderEvent.quantity,
            submitOrderEvent.quantity - submitOrderEvent.remainedQuantity,
            submitOrderEvent.price.toBigDecimal(),
            submitOrderEvent.quantity.toBigDecimal(),
            (submitOrderEvent.quantity - submitOrderEvent.remainedQuantity).toBigDecimal(),
            financialActions[0].amount,
            financialActions[0].amount,
            0
        )
    }
}