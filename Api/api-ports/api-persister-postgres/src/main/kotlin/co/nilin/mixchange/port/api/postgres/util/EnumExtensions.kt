package co.nilin.mixchange.port.api.postgres.util

import co.nilin.mixchange.api.core.inout.OrderSide
import co.nilin.mixchange.api.core.inout.OrderStatus
import co.nilin.mixchange.api.core.inout.TimeInForce
import co.nilin.mixchange.matching.core.model.MatchConstraint
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.OrderType

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

fun OrderType.toApiOrderType(): co.nilin.mixchange.api.core.inout.OrderType {
    if (this == OrderType.LIMIT_ORDER)
        return co.nilin.mixchange.api.core.inout.OrderType.LIMIT
    if (this == OrderType.MARKET_ORDER)
        return co.nilin.mixchange.api.core.inout.OrderType.MARKET
    throw IllegalArgumentException("OrderType $this is not supported!")
}

fun OrderDirection.toOrderSide(): OrderSide {
    if (this == OrderDirection.ASK)
        return OrderSide.BUY
    return OrderSide.SELL
}

fun OrderStatus.isWorking(): Boolean {
    return listOf<OrderStatus>(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED).contains(this)
}

fun Int.toOrderStatus(): OrderStatus {
    val status = co.nilin.mixchange.accountant.core.inout.OrderStatus.values()
            .find { s -> s.code == this }
    return OrderStatus.valueOf(status!!.name)
}