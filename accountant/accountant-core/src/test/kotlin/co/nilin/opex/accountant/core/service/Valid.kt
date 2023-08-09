package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair
import java.math.BigDecimal
import java.time.LocalDateTime

object Valid {

    val currentTime = LocalDateTime.now()

    val fa = FinancialAction(
        null,
        TradeEvent::class.java.name,
        "trade_id",
        "BTC_USDT",
        10000.0.toBigDecimal(),
        "user_parent",
        "main",
        "system",
        "main",
        currentTime,
        FinancialActionCategory.ORDER_CREATE,
        emptyMap()
    )

    val faHighRetry = FinancialAction(
        null,
        TradeEvent::class.java.name,
        "trade_id",
        "BTC_USDT",
        10000.0.toBigDecimal(),
        "user_parent",
        "main",
        "system",
        "main",
        currentTime,
        FinancialActionCategory.ORDER_CREATE,
        emptyMap(),
        id = 15
    )

    var makerOrder = Order(
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

    var takerOrder = Order(
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

    var tradeEvent = TradeEvent(
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

    val order = Order(
        "BTC_USDT",
        "order_ouid",
        null,
        0.01.toBigDecimal(),
        0.01.toBigDecimal(),
        0.000001.toBigDecimal(),
        0.01.toBigDecimal(),
        "user_1",
        "*",
        OrderDirection.BID,
        MatchConstraint.GTC,
        OrderType.LIMIT_ORDER,
        100000,
        1000,
        0,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        100000.0.toBigDecimal(),
        OrderStatus.NEW.code
    )

}