package co.nilin.mixchange.api.core.spi

import co.nilin.mixchange.matching.core.model.MatchConstraint
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.OrderType
import java.math.BigDecimal

interface MEGatewayProxy {
    data class CreateOrderRequest(
            var uuid: String?,
            val pair: String,
            val price: BigDecimal,
            val quantity: BigDecimal,
            val direction: OrderDirection,
            val matchConstraint: MatchConstraint,
            val orderType: OrderType
    )

    class OrderSubmitResult(offset: Long?)

    fun createNewOrder(order: CreateOrderRequest): OrderSubmitResult;
}