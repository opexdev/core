package co.nilin.opex.port.api.binance.util

import co.nilin.opex.api.core.inout.OrderSide
import co.nilin.opex.api.core.inout.TimeInForce
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType

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

fun co.nilin.opex.api.core.inout.OrderType.asMatchingOrderType(): OrderType {
    return when (this) {
        co.nilin.opex.api.core.inout.OrderType.LIMIT -> OrderType.LIMIT_ORDER
        co.nilin.opex.api.core.inout.OrderType.MARKET -> OrderType.MARKET_ORDER
        else -> OrderType.LIMIT_ORDER
    }
}

fun <T, R : Enum<T>> R.equalsAny(vararg equals: R): Boolean {
    for (e in equals)
        if (this == e)
            return true
    return false
}