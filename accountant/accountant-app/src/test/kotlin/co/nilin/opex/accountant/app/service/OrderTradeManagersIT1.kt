package co.nilin.opex.accountant.app.service

import co.nilin.opex.accountant.app.KafkaEnabledTest
import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.OrderPersister
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.*

class OrderTradeManagersIT1 : KafkaEnabledTest() {
    @Autowired
    lateinit var orderManager: OrderManager

    @Autowired
    lateinit var tradeManager: TradeManager

    @Autowired
    lateinit var orderPersister: OrderPersister

    @Autowired
    lateinit var financialActionLoader: FinancialActionLoader

    @Test
    fun whenOrderFinalizedWithLowerPrice_expectExtraBlockedReleased() {
        runBlocking {
            val pair = Pair("TBTC", "TUSDT")
            val bidOrderEvent = SubmitOrderEvent(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1L, pair, 1100, 1000000, 1000000, OrderDirection.BID, MatchConstraint.GTC, OrderType.LIMIT_ORDER, ""
            )
            orderManager.handleRequestOrder(bidOrderEvent)


            val askOrderEvent = SubmitOrderEvent(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), 2L, pair, 1000, 1000000, 1000000, OrderDirection.ASK, MatchConstraint.GTC, OrderType.LIMIT_ORDER, ""
            )
            orderManager.handleRequestOrder(askOrderEvent)

            val tradeEvent = TradeEvent(
                1L, pair,
                bidOrderEvent.ouid, bidOrderEvent.uuid,
                bidOrderEvent.orderId!!, bidOrderEvent.direction,
                bidOrderEvent.price, bidOrderEvent.remainedQuantity - askOrderEvent.quantity,
                askOrderEvent.ouid, askOrderEvent.uuid,
                askOrderEvent.orderId!!, askOrderEvent.direction,
                askOrderEvent.price, askOrderEvent.remainedQuantity - askOrderEvent.quantity,
                askOrderEvent.quantity
            )
            tradeManager.handleTrade(tradeEvent)

            val bidOrderDB = orderPersister.load(bidOrderEvent.ouid)!!
            Assertions.assertTrue(BigDecimal.ZERO.compareTo(bidOrderDB.remainedTransferAmount) == 0)
            Assertions.assertEquals(bidOrderDB.filledOrigQuantity, bidOrderDB.origQuantity)
            Assertions.assertEquals(bidOrderDB.filledQuantity, bidOrderDB.quantity)

            val askOrderDB = orderPersister.load(askOrderEvent.ouid)!!
            Assertions.assertTrue(BigDecimal.ZERO.compareTo(askOrderDB.remainedTransferAmount) == 0)
            Assertions.assertEquals(askOrderDB.filledOrigQuantity, askOrderDB.origQuantity)
            Assertions.assertEquals(askOrderDB.filledQuantity, askOrderDB.quantity)

            assertFinancialActions(bidOrderDB, askOrderDB)
        }

    }

    @Test
    fun whenOrderFinalizedWithLowerPriceAndNotOrderedTrade_expectExtraBlockedReleased() {
        runBlocking {
            val pair = Pair("TBTC", "TUSDT")
            val bidOrderEvent = SubmitOrderEvent(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1L, pair, 1100, 1000000, 1000000, OrderDirection.BID, MatchConstraint.GTC, OrderType.LIMIT_ORDER, ""
            )
            orderManager.handleRequestOrder(bidOrderEvent)


            val askOrderEvent = SubmitOrderEvent(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), 2L, pair, 1000, 1000000, 1000000, OrderDirection.ASK, MatchConstraint.GTC, OrderType.LIMIT_ORDER, ""
            )
            orderManager.handleRequestOrder(askOrderEvent)

            val firstMatch = 600000L
            val secondMatch = 400000L

            val secondTradeEvent = TradeEvent(
                1L, pair,
                bidOrderEvent.ouid, bidOrderEvent.uuid,
                bidOrderEvent.orderId!!, bidOrderEvent.direction,
                bidOrderEvent.price, bidOrderEvent.remainedQuantity - firstMatch - secondMatch,
                askOrderEvent.ouid, askOrderEvent.uuid,
                askOrderEvent.orderId!!, askOrderEvent.direction,
                askOrderEvent.price, askOrderEvent.remainedQuantity - firstMatch - secondMatch,
                secondMatch
            )
            tradeManager.handleTrade(secondTradeEvent)

            val firstTradeEvent = TradeEvent(
                1L, pair,
                bidOrderEvent.ouid, bidOrderEvent.uuid,
                bidOrderEvent.orderId!!, bidOrderEvent.direction,
                bidOrderEvent.price, bidOrderEvent.remainedQuantity - firstMatch,
                askOrderEvent.ouid, askOrderEvent.uuid,
                askOrderEvent.orderId!!, askOrderEvent.direction,
                askOrderEvent.price, askOrderEvent.remainedQuantity - firstMatch,
                firstMatch
            )
            tradeManager.handleTrade(firstTradeEvent)

            val bidOrderDB = orderPersister.load(bidOrderEvent.ouid)!!
            Assertions.assertTrue(BigDecimal.ZERO.compareTo(bidOrderDB.remainedTransferAmount) == 0)
            Assertions.assertEquals(bidOrderDB.filledOrigQuantity, bidOrderDB.origQuantity)
            Assertions.assertEquals(bidOrderDB.filledQuantity, bidOrderDB.quantity)
            Assertions.assertEquals(OrderStatus.FILLED.code, bidOrderDB.status)

            val askOrderDB = orderPersister.load(askOrderEvent.ouid)!!
            Assertions.assertTrue(BigDecimal.ZERO.compareTo(askOrderDB.remainedTransferAmount) == 0)
            Assertions.assertEquals(askOrderDB.filledOrigQuantity, askOrderDB.origQuantity)
            Assertions.assertEquals(askOrderDB.filledQuantity, askOrderDB.quantity)
            Assertions.assertEquals(OrderStatus.FILLED.code, askOrderDB.status)

            assertFinancialActions(bidOrderDB, askOrderDB)
        }

    }

    private suspend fun assertFinancialActions(bidOrderDB: Order, askOrderDB: Order) {
        val financialActions = financialActionLoader.loadUnprocessed(0, 10)

        val bidOrderSubmitFi = financialActions.stream().filter { fi ->
            fi.category == FinancialActionCategory.ORDER_CREATE
                    && fi.detail["ouid"] == bidOrderDB.ouid
        }.findAny().orElse(null)
        Assertions.assertNotNull(bidOrderSubmitFi)
        assertTotalAmount(financialActions, bidOrderDB, bidOrderSubmitFi)

        val bidOrderFinalizeFi = financialActions.stream().filter { fi ->
            fi.category == FinancialActionCategory.ORDER_FINALIZED
                    && fi.detail["ouid"] == bidOrderDB.ouid
                    && fi.sender == bidOrderDB.uuid
        }.findAny().orElse(null)
        Assertions.assertNotNull(bidOrderFinalizeFi)
        Assertions.assertEquals(bidOrderFinalizeFi.parent, bidOrderSubmitFi)
        Assertions.assertEquals(bidOrderFinalizeFi.symbol, bidOrderSubmitFi.symbol)

        val askOrderSubmitFi = financialActions.stream().filter { fi ->
            fi.category == FinancialActionCategory.ORDER_CREATE
                    && fi.detail["ouid"] == askOrderDB.ouid
        }.findAny().orElse(null)
        Assertions.assertNotNull(askOrderSubmitFi)

        val askOrderFinalizeFi = financialActions.stream().filter { fi ->
            fi.category == FinancialActionCategory.ORDER_FINALIZED
                    && fi.detail["ouid"] == askOrderDB.ouid
        }.findAny().orElse(null)
        Assertions.assertNull(askOrderFinalizeFi)


        assertTotalAmount(financialActions, askOrderDB, askOrderSubmitFi)
    }

    private fun assertTotalAmount(financialActions: List<FinancialAction>, askOrderDB: Order, askOrderSubmitFi: FinancialAction) {
        val askFiSide1 = financialActions.filter { fi ->
            fi.detail["ouid"] == askOrderDB.ouid
                    && fi.symbol == askOrderSubmitFi.symbol
                    && fi.sender == askOrderDB.uuid
        }
        val askTotalFiAmountSide1 = askFiSide1.sumOf { fi ->
            if (fi.senderWalletType.equals("main"))
                fi.amount
            else
                -fi.amount
        }
        Assertions.assertTrue(BigDecimal.ZERO.compareTo(askTotalFiAmountSide1) == 0)
    }
}