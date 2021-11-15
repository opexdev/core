package co.nilin.opex.matching.engine.ports.kafka.listener.inout

import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import co.nilin.opex.matching.core.model.Pair

class OrderSubmitRequest() {

    lateinit var ouid: String
    lateinit var uuid: String
    var orderId: Long? = null
    lateinit var pair: Pair
    var price: Long = 0
    var quantity: Long = 0
    var direction: OrderDirection = OrderDirection.BID
    var matchConstraint: MatchConstraint = MatchConstraint.GTC
    var orderType: OrderType = OrderType.LIMIT_ORDER


    constructor(
        ouid: String,
        uuid: String,
        orderId: Long?,
        pair: Pair,
        price: Long,
        quantity: Long,
        direction: OrderDirection,
        matchConstraint: MatchConstraint,
        orderType: OrderType
    ) : this() {
        this.ouid = ouid
        this.uuid = uuid
        this.orderId = orderId
        this.pair = pair
        this.price = price
        this.quantity = quantity
        this.direction = direction
        this.matchConstraint = matchConstraint
        this.orderType = orderType
    }


}