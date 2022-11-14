package co.nilin.opex.api.ports.proxy.data

import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import java.math.BigDecimal

data class CreateOrderRequest(
    var uuid: String?,
    val pair: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val direction: OrderDirection,
    val matchConstraint: MatchConstraint?,
    val orderType: MatchingOrderType,
    val userLevel: String
)