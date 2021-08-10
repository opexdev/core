package co.nilin.opex.port.api.binance.util

import co.nilin.opex.api.core.inout.OrderSide
import co.nilin.opex.api.core.inout.TimeInForce
import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderType
import co.nilin.opex.matching.core.model.OrderDirection

fun OrderSide.asOrderDirection(): OrderDirection {
    if (this == OrderSide.BUY)
        return OrderDirection.ASK
    return OrderDirection.BID
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