package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.model.*
import co.nilin.opex.accountant.ports.postgres.model.*
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import java.math.BigDecimal
import java.time.LocalDateTime

object Valid {

    private val currentTime = LocalDateTime.now()

    val orderModel = OrderModel(
        1,
        "order_1",
        "user_1",
        "BTC_USDT",
        1,
        0.01.toBigDecimal(),
        0.01.toBigDecimal(),
        0.00001.toBigDecimal(),
        0.01.toBigDecimal(),
        "",
        OrderDirection.BID,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        5500000,
        100,
        100,
        55000.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        OrderStatus.FILLED.code,
        "",
        "",
        currentTime
    )

    val order = Order(
        "BTC_USDT",
        "order_1",
        1,
        0.01.toBigDecimal(),
        0.01.toBigDecimal(),
        0.00001.toBigDecimal(),
        0.01.toBigDecimal(),
        "user_1",
        "",
        OrderDirection.BID,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        5500000,
        100,
        100,
        55000.0.toBigDecimal(),
        1.0.toBigDecimal(),
        1.0.toBigDecimal(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        OrderStatus.FILLED.code,
        1
    )

    val pairConfigModel = PairConfigModel(
        "BTC_USDT",
        "BTC",
        "USDT",
        0.000001.toBigDecimal(),
        0.01.toBigDecimal()
    )

    val pairConfig = PairConfig(
        "BTC_USDT",
        "BTC",
        "USDT",
        0.000001.toBigDecimal(),
        0.01.toBigDecimal()
    )

    class TestCoreEvent(val leftSidePair: String, val rightSidePair: String) :
        CoreEvent(Pair(leftSidePair, rightSidePair), currentTime)

    val testEvent = TestCoreEvent("BTC", "USDT")

    val tempEvent = TempEvent(
        1,
        "event_1",
        TestCoreEvent("BTC", "USDT"),
        currentTime
    )

    val tempEventModel = TempEventModel(
        1,
        "event_1",
        TestCoreEvent::class.java.name,
        "{\"leftSidePair\":\"BTC\",\"rightSidePair\":\"USDT\",\"pair\":{\"leftSideName\":\"BTC\",\"rightSideName\":\"USDT\"},\"eventDate\":[2023,9,30,15,56,11,210882700]}",
        currentTime
    )

    val fa = FinancialAction(
        null,
        TradeEvent::class.java.name,
        "trade_id",
        "BTC_USDT",
        10000.0.toBigDecimal(),
        "user_parent",
        WalletType.MAIN,
        "system",
        WalletType.MAIN,
        currentTime,
        FinancialActionCategory.TRADE,
        id = 1,
        uuid = "uuid"
    )

    val faModel = FinancialActionModel(
        null,
        "uuid",
        null,
        TradeEvent::class.java.name,
        "trade_id",
        "BTC_USDT",
        10000.0.toBigDecimal(),
        "user_parent",
        WalletType.MAIN,
        "system",
        WalletType.MAIN,
        FinancialActionCategory.TRADE,
        "",
        "",
        currentTime,
    )

}