package co.nilin.mixchange.api.core.spi

import co.nilin.mixchange.api.core.inout.OrderSubmitResult
import co.nilin.mixchange.matching.core.model.MatchConstraint
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.OrderType
import reactor.core.publisher.Mono
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
}