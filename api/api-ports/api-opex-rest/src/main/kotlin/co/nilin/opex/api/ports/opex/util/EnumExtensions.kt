package co.nilin.opex.api.ports.opex.util

import co.nilin.opex.api.core.inout.*

fun OrderSide.asOrderDirection(): OrderDirection {
    if (this == OrderSide.BUY)
        return OrderDirection.BID
    return OrderDirection.ASK
}

fun OrderDirection.asOrderSide(): OrderSide {
    if (this == OrderDirection.BID)
        return OrderSide.BUY
    return OrderSide.SELL
}

fun TimeInForce.asMatchConstraint(): MatchConstraint {
    return when (this) {
        TimeInForce.GTC -> MatchConstraint.GTC
        TimeInForce.IOC -> MatchConstraint.IOC
        TimeInForce.FOK -> MatchConstraint.FOK
    }
}

fun MatchConstraint.asTimeInForce(): TimeInForce {
    return when (this) {
        MatchConstraint.GTC -> TimeInForce.GTC
        MatchConstraint.IOC -> TimeInForce.IOC
        MatchConstraint.FOK -> TimeInForce.FOK
        else -> TimeInForce.GTC
    }
}

fun OrderType.asMatchingOrderType(): MatchingOrderType {
    return when (this) {
        OrderType.LIMIT -> MatchingOrderType.LIMIT_ORDER
        OrderType.MARKET -> MatchingOrderType.MARKET_ORDER
        else -> MatchingOrderType.LIMIT_ORDER
    }
}

fun MatchingOrderType.asOrderType(): OrderType {
    return when (this) {
        MatchingOrderType.LIMIT_ORDER -> OrderType.LIMIT
        MatchingOrderType.MARKET_ORDER -> OrderType.MARKET
    }
}

fun <T, R : Enum<T>> R.equalsAny(vararg equals: R): Boolean {
    for (e in equals)
        if (this == e)
            return true
    return false
}