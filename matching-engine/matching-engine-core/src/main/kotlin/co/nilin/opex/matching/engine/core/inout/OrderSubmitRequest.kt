package co.nilin.opex.matching.engine.core.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair

class OrderSubmitRequest(
    var ouid: String,
    var uuid: String,
    var pair: Pair,
    var orderId: Long? = null,
    var price: Long = 0,
    var quantity: Long = 0,
    var direction: OrderDirection = OrderDirection.BID,
    var matchConstraint: MatchConstraint = MatchConstraint.GTC,
    var orderType: OrderType = OrderType.LIMIT_ORDER,
    var userLevel: String = ""
)