package co.nilin.opex.api.ports.binance.util

import co.nilin.opex.api.core.inout.*

fun OrderSide.asOrderDirection(): OrderDirection {
    if (this == OrderSide.BUY)
        return OrderDirection.BID
    return OrderDirection.ASK
}

fun TimeInForce.asMatchConstraint(): MatchConstraint {
    return when (this) {
        TimeInForce.GTC -> MatchConstraint.GTC
        TimeInForce.IOC -> MatchConstraint.IOC
        TimeInForce.FOK -> MatchConstraint.FOK
    }
}

fun OrderType.asMatchingOrderType(): MatchingOrderType {
    return when (this) {
        OrderType.LIMIT -> MatchingOrderType.LIMIT_ORDER
        OrderType.MARKET -> MatchingOrderType.MARKET_ORDER
        else -> MatchingOrderType.LIMIT_ORDER
    }
}

fun <T, R : Enum<T>> R.equalsAny(vararg equals: R): Boolean {
    for (e in equals)
        if (this == e)
            return true
    return false
}