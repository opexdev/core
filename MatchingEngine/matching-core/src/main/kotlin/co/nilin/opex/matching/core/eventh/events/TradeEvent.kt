package co.nilin.opex.matching.core.eventh.events

import co.nilin.opex.matching.core.model.OrderDirection

class TradeEvent() : CoreEvent() {
    var tradeId: Long = 0
    var takerOuid: String = ""
    var takerUuid: String = ""
    var takerOrderId: Long = 0
    var takerDirection: OrderDirection = OrderDirection.ASK
    var takerPrice: Long = 0
    var takerRemainedQuantity: Long = 0
    var makerOuid: String = ""
    var makerUuid: String = ""
    var makerOrderId: Long = 0
    var makerDirection: OrderDirection = OrderDirection.BID
    var makerPrice: Long = 0
    var makerRemainedQuantity: Long = 0
    var matchedQuantity: Long = 0


    constructor(tradeId: Long,
                pair: co.nilin.opex.matching.core.model.Pair,
                takerOuid: String,
                takerUuid: String,
                takerOrderId: Long,
                takerDirection: OrderDirection,
                takerPrice: Long,
                takerRemainedQuantity: Long,
                makerOuid: String,
                makerUuid: String,
                makerOrderId: Long,
                makerDirection: OrderDirection,
                makerPrice: Long,
                makerRemainedQuantity: Long,
                matchedQuantity: Long
    )
            : this() {
        this.tradeId = tradeId
        this.takerOuid = takerOuid
        this.takerUuid = takerUuid
        this.pair = pair
        this.takerOrderId = takerOrderId
        this.takerPrice = takerPrice
        this.takerDirection = takerDirection
        this.takerRemainedQuantity = takerRemainedQuantity

        this.makerOuid = makerOuid
        this.makerUuid = makerUuid
        this.makerOrderId = makerOrderId
        this.makerPrice = makerPrice
        this.makerDirection = makerDirection
        this.makerRemainedQuantity = makerRemainedQuantity

        this.matchedQuantity = matchedQuantity
    }
}