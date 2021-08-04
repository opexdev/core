package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.accountant.core.model.PairFeeConfig
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.matching.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.core.eventh.events.TradeEvent
import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import co.nilin.opex.matching.core.model.Pair
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers

import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

internal class TradeManagerImplTest() {
    @Mock
    lateinit var financialActionPersister: FinancialActionPersister

    @Mock
    lateinit var financeActionLoader: FinancialActionLoader

    @Mock
    lateinit var orderPersister: OrderPersister

    @Mock
    lateinit var pairConfigLoader: PairConfigLoader

    @Mock
    lateinit var pairStaticRateLoader: PairStaticRateLoader

    @Mock
    lateinit var walletProxy: WalletProxy

    @Mock
    lateinit var tempEventPersister: TempEventPersister

    @Mock
    lateinit var tempEventRepublisher: TempEventRepublisher

    @Mock
    lateinit var richOrderPublisher: RichOrderPublisher

    @Mock
    lateinit var richTradePublisher: RichTradePublisher

    val orderManager: OrderManager

    val tradeManager: TradeManager


    init {
        MockitoAnnotations.openMocks(this)
        orderManager = OrderManagerImpl(
                pairConfigLoader,
                financialActionPersister,
                financeActionLoader,
                orderPersister,
                tempEventPersister,
                tempEventRepublisher,
                richOrderPublisher
        )
        tradeManager = TradeManagerImpl(
                pairStaticRateLoader,
                financialActionPersister,
                financeActionLoader,
                orderPersister,
                tempEventPersister,
                richTradePublisher,
                walletProxy,
                "pcoin",
                "0x0"
        )
        runBlocking {
            Mockito.`when`(tempEventPersister.loadTempEvents(ArgumentMatchers.anyString())).thenReturn(emptyList())
        }
    }

    @Test
    fun givenMatchOrders_whenTradeCreated_thenFAMatched() {
        runBlocking {
            //given
            val pair = Pair("eth", "btc")
            val pairConfig = co.nilin.opex.accountant.core.model.PairConfig(
                pair.toString(), pair.leftSideName, pair.rightSideName, 1.0, 0.001
            )
            val makerSubmitOrderEvent = SubmitOrderEvent(
                "mouid", "muuid", null, pair, 29, 60, 0, OrderDirection.ASK, MatchConstraint.GTC, OrderType.LIMIT_ORDER
            )
            prepareOrder(pair, pairConfig, makerSubmitOrderEvent, 0.1, 0.12)

            val takerSubmitOrderEvent = SubmitOrderEvent(
                "touid", "tuuid", null, pair, 30, 14, 0, OrderDirection.BID, MatchConstraint.GTC, OrderType.LIMIT_ORDER
            )

            prepareOrder(pair, pairConfig, takerSubmitOrderEvent, 0.08, 0.1)

            val tradeEvent = makeTradeEvent(pair, takerSubmitOrderEvent, makerSubmitOrderEvent)
            //when
            val tradeFinancialActions = tradeManager.handleTrade(tradeEvent)
            Assertions.assertEquals(4, tradeFinancialActions.size)
        }
    }

    @Test
    fun givenMatchOrders2_whenTradeCreated_thenFAMatched() {
        runBlocking {
            //given
            val pair = Pair("eth", "btc")
            val pairConfig = co.nilin.opex.accountant.core.model.PairConfig(
                pair.toString(), pair.leftSideName, pair.rightSideName, 0.000001, 0.000001
            )
            val makerSubmitOrderEvent = SubmitOrderEvent(
                "mouid",
                "muuid",
                null,
                pair,
                33333,
                5000000,
                0,
                OrderDirection.ASK,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )
            prepareOrder(pair, pairConfig, makerSubmitOrderEvent, 0.8 * 0.001, 1.0 * 0.001)

            val takerSubmitOrderEvent = SubmitOrderEvent(
                "touid",
                "tuuid",
                null,
                pair,
                34482,
                1000000,
                0,
                OrderDirection.BID,
                MatchConstraint.GTC,
                OrderType.LIMIT_ORDER
            )

            prepareOrder(pair, pairConfig, takerSubmitOrderEvent, 0.8 * 0.001, 1.0 * 0.001)

            val tradeEvent = makeTradeEvent(pair, takerSubmitOrderEvent, makerSubmitOrderEvent)
            //when
            val tradeFinancialActions = tradeManager.handleTrade(tradeEvent)
            Assertions.assertEquals(4, tradeFinancialActions.size)
        }
    }

    private fun makeTradeEvent(
        pair: Pair,
        takerSubmitOrderEvent: SubmitOrderEvent,
        makerSubmitOrderEvent: SubmitOrderEvent
    ): TradeEvent {
        val tradeEvent = TradeEvent(
                0,
                pair,
                takerSubmitOrderEvent.ouid,
                takerSubmitOrderEvent.uuid,
                takerSubmitOrderEvent.orderId ?: -1,
                takerSubmitOrderEvent.direction,
                takerSubmitOrderEvent.price,
                0,
                makerSubmitOrderEvent.ouid,
                makerSubmitOrderEvent.uuid,
                makerSubmitOrderEvent.orderId ?: 1,
                makerSubmitOrderEvent.direction,
                makerSubmitOrderEvent.price,
                makerSubmitOrderEvent.quantity - takerSubmitOrderEvent.quantity,
                takerSubmitOrderEvent.quantity
        )
        return tradeEvent
    }

    private fun prepareOrder(
        pair: Pair,
        pairConfig: co.nilin.opex.accountant.core.model.PairConfig,
        submitOrderEvent: SubmitOrderEvent,
        makerFee: Double,
        takerFee: Double
    ) {
        runBlocking {
            Mockito.`when`(pairConfigLoader.load(pair.toString(), submitOrderEvent.direction, ""))
                .thenReturn(
                    co.nilin.opex.accountant.core.model.PairFeeConfig(
                        pairConfig,
                        submitOrderEvent.direction.toString(),
                        "",
                        makerFee,
                        takerFee
                    )
                )
            Mockito.`when`(financialActionPersister.persist(MockitoHelper.anyObject()))
                .then {
                    return@then it.getArgument<List<FinancialAction>>(0)
                }

            val financialActions = orderManager.handleRequestOrder(submitOrderEvent)

            val orderPairFeeConfig =
                pairConfigLoader.load(submitOrderEvent.pair.toString(), submitOrderEvent.direction, "")
            val orderMakerFee = orderPairFeeConfig.makerFee * 1 //user level formula
            val orderTakerFee = orderPairFeeConfig.takerFee * 1 //user level formula
            Mockito.`when`(orderPersister.load(submitOrderEvent.ouid)).thenReturn(
                Order(
                    submitOrderEvent.pair.toString(),
                    submitOrderEvent.ouid,
                    null,
                    orderMakerFee,
                    orderTakerFee,
                    orderPairFeeConfig.pairConfig.leftSideFraction,
                    orderPairFeeConfig.pairConfig.rightSideFraction,
                    submitOrderEvent.uuid,
                    "",
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
            )
        }
    }
}