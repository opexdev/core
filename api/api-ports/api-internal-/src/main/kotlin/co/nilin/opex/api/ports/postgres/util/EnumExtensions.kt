package co.nilin.opex.api.ports.postgres.util

import co.nilin.opex.api.core.inout.*

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

fun MatchingOrderType.toApiOrderType(): OrderType {
    if (this == MatchingOrderType.LIMIT_ORDER)
        return OrderType.LIMIT
    if (this == MatchingOrderType.MARKET_ORDER)
        return OrderType.MARKET
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
    val status = OrderStatus.values().find { s -> s.code == this }
    return OrderStatus.valueOf(status!!.name)
}