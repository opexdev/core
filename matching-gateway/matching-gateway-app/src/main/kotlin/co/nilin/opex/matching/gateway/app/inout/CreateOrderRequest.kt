package co.nilin.opex.matching.gateway.app.inout

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
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