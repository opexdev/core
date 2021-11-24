package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.inout.RejectReason
import co.nilin.opex.matching.engine.core.inout.RequestedOperation
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType

class RejectOrderEvent() : CoreEvent(), OneOrderEvent {
    var ouid: String = ""
    var uuid: String = ""
    var orderId: Long? = null
    var price: Long? = null
    var quantity: Long? = null
    var direction: OrderDirection? = null
    var matchConstraint: MatchConstraint? = null
    var orderType: OrderType? = null
    var requestedOperation: RequestedOperation = RequestedOperation.PLACE_ORDER
    var reason: RejectReason? = null

    constructor(
        ouid: String,
        uuid: String,
        pair: co.nilin.opex.matching.engine.core.model.Pair,
        price: Long,
        quantity: Long,
        direction: OrderDirection,
        matchConstraint: MatchConstraint,
        orderType: OrderType,
        requestedOperation: RequestedOperation,
        reason: RejectReason?
    )
            : this(
        ouid,
        uuid,
        null,
        pair,
        price,
        quantity,
        direction,
        matchConstraint,
        orderType,
        requestedOperation,
        reason
    )

    constructor(
        ouid: String,
        uuid: String,
        orderId: Long,
        pair: co.nilin.opex.matching.engine.core.model.Pair,
        requestedOperation: RequestedOperation,
        reason: RejectReason?
    )
            : this(ouid, uuid, orderId, pair, null, null, null, null, null, requestedOperation, reason)

    constructor(
        ouid: String,
        uuid: String,
        orderId: Long?,
        pair: co.nilin.opex.matching.engine.core.model.Pair,
        price: Long?,
        quantity: Long?,
        direction: OrderDirection?,
        matchConstraint: MatchConstraint?,
        orderType: OrderType?,
        requestedOperation: RequestedOperation,
        reason: RejectReason?
    )
            : this() {
        this.ouid = ouid
        this.uuid = uuid
        this.orderId = orderId
        this.pair = pair
        this.price = price
        this.quantity = quantity
        this.direction = direction
        this.matchConstraint = matchConstraint
        this.orderType = orderType
        this.requestedOperation = requestedOperation
        this.reason = reason
    }

    override fun ouid(): String {
        return ouid
    }

    override fun uuid(): String {
        return uuid
    }
}