package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.CancelOrderRequest
import co.nilin.opex.api.core.inout.OrderSubmitResult
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import java.math.BigDecimal

interface MEGatewayProxy {
    data class CreateOrderRequest(
        var uuid: String?,
        val pair: String,
        val price: BigDecimal,
        val quantity: BigDecimal,
        val direction: OrderDirection,
        val matchConstraint: MatchConstraint?,
        val orderType: OrderType
    )

    suspend fun createNewOrder(order: CreateOrderRequest, token: String?): OrderSubmitResult?

    suspend fun cancelOrder(request: CancelOrderRequest, token: String?): OrderSubmitResult?
}