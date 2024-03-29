package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import co.nilin.opex.matching.engine.core.model.Pair

class SubmitOrderEvent(
    var ouid: String = "",
    var uuid: String = "",
    var orderId: Long? = null,
    pair: Pair,
    var price: Long = 0,
    var quantity: Long = 0,
    var remainedQuantity: Long = 0,
    var direction: OrderDirection = OrderDirection.ASK,
    var matchConstraint: MatchConstraint = MatchConstraint.GTC,
    var orderType: OrderType = OrderType.LIMIT_ORDER,
    val userLevel: String = ""
) : CoreEvent(pair), OneOrderEvent {

    override fun ouid(): String {
        return ouid
    }

    override fun uuid(): String {
        return uuid
    }
}