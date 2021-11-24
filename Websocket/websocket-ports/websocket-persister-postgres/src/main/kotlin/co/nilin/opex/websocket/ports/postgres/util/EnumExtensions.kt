package co.nilin.opex.websocket.ports.postgres.util

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.websocket.core.inout.OrderSide
import co.nilin.opex.websocket.core.inout.OrderStatus
import co.nilin.opex.websocket.core.inout.TimeInForce

fun MatchConstraint.toTimeInForce(): TimeInForce {
    if (this == MatchConstraint.FOK_BUDGET)
        return TimeInForce.FOK
    if (this == MatchConstraint.IOC_BUDGET)
        return TimeInForce.IOC
    return TimeInForce.valueOf(this.name)
}


fun TimeInForce.toMatchConstraint(): MatchConstraint {
    return MatchConstraint.valueOf(this.name)
}

fun OrderType.toWebSocketOrderType(): co.nilin.opex.websocket.core.inout.OrderType {
    if (this == OrderType.LIMIT_ORDER)
        return co.nilin.opex.websocket.core.inout.OrderType.LIMIT
    if (this == OrderType.MARKET_ORDER)
        return co.nilin.opex.websocket.core.inout.OrderType.MARKET
    throw IllegalArgumentException("OrderType $this is not supported!")
}

fun OrderDirection.toOrderSide(): OrderSide {
    if (this == OrderDirection.BID)
        return OrderSide.BUY
    return OrderSide.SELL
}

fun OrderStatus.isWorking(): Boolean {
    return listOf(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED).contains(this)
}

fun Int.toOrderStatus(): OrderStatus {
    val status = co.nilin.opex.accountant.core.inout.OrderStatus.values()
        .find { s -> s.code == this }
    return OrderStatus.valueOf(status!!.name)
}