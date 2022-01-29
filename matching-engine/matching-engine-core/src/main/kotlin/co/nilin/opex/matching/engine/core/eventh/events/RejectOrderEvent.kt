package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.inout.RejectReason
import co.nilin.opex.matching.engine.core.inout.RequestedOperation
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair

class RejectOrderEvent(
    var ouid: String = "",
    var uuid: String = "",
    var orderId: Long? = null,
    pair: Pair,
    var price: Long? = null,
    var quantity: Long? = null,
    var direction: OrderDirection? = null,
    var matchConstraint: MatchConstraint? = null,
    var orderType: OrderType? = null,
    var requestedOperation: RequestedOperation = RequestedOperation.PLACE_ORDER,
    var reason: RejectReason? = null,
) : CoreEvent(pair), OneOrderEvent {

    constructor(
        ouid: String,
        uuid: String,
        pair: Pair,
        price: Long,
        quantity: Long,
        direction: OrderDirection,
        matchConstraint: MatchConstraint,
        orderType: OrderType,
        requestedOperation: RequestedOperation,
        reason: RejectReason?
    ) : this(ouid, uuid, null, pair, price, quantity, direction, matchConstraint, orderType, requestedOperation, reason)

    constructor(
        ouid: String,
        uuid: String,
        orderId: Long,
        pair: Pair,
        requestedOperation: RequestedOperation,
        reason: RejectReason?
    ) : this(ouid, uuid, orderId, pair, null, null, null, null, null, requestedOperation, reason)

    override fun ouid(): String {
        return ouid
    }

    override fun uuid(): String {
        return uuid
    }
}