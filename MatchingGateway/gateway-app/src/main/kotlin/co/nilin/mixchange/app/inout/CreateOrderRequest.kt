package co.nilin.mixchange.app.inout

import co.nilin.mixchange.matching.core.model.MatchConstraint
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.OrderType
import java.math.BigDecimal

data class CreateOrderRequest(
    var uuid: String?,
    val pair: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType
)