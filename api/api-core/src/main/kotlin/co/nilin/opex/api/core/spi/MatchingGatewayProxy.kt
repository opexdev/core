package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderSubmitResult
import java.math.BigDecimal

interface MatchingGatewayProxy {

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

    suspend fun cancelOrder(
        ouid: String,
        uuid: String,
        orderId: Long,
        symbol: String,
        token: String?
    ): OrderSubmitResult?
}