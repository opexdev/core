package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.Pair

class TradeEvent(
    var tradeId: Long = 0,
    pair: Pair,
    var takerOuid: String = "",
    var takerUuid: String = "",
    var takerOrderId: Long = 0,
    var takerDirection: OrderDirection = OrderDirection.ASK,
    var takerPrice: Long = 0,
    var takerRemainedQuantity: Long = 0,
    var makerOuid: String = "",
    var makerUuid: String = "",
    var makerOrderId: Long = 0,
    var makerDirection: OrderDirection = OrderDirection.BID,
    var makerPrice: Long = 0,
    var makerRemainedQuantity: Long = 0,
    var matchedQuantity: Long = 0
) : CoreEvent(pair)