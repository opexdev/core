package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.*
import java.math.BigDecimal

interface MEGatewayProxy {

    data class CreateOrderRequest(
        var uuid: String?,
        val pair: String,
        val price: BigDecimal,
        val quantity: BigDecimal,
        val direction: OrderDirection,
        val matchConstraint: MatchConstraint?,
        val orderType: MatchingOrderType
    )

    suspend fun createNewOrder(order: CreateOrderRequest, token: String?): OrderSubmitResult?

    suspend fun cancelOrder(request: CancelOrderRequest, token: String?): OrderSubmitResult?
}