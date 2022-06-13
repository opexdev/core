package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.AllOrderRequest
import co.nilin.opex.market.core.inout.Order
import co.nilin.opex.market.core.inout.QueryOrderResponse
import co.nilin.opex.market.core.spi.MarketQueryHandler
import co.nilin.opex.market.core.spi.OrderPersister
import co.nilin.opex.market.core.spi.UserQueryHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/order")
class OrderController(
    private val marketQueryHandler: MarketQueryHandler,
    private val userQueryHandler: UserQueryHandler,
    private val orderPersister: OrderPersister,
) {

    @GetMapping("/{ouid}")
    suspend fun getOrderById(@PathVariable ouid: String): Order {
        return orderPersister.load(ouid) ?: throw OpexException(OpexError.NotFound)
    }

    @PostMapping("/user/{uuid}")
    suspend fun getUserOrders(
        @PathVariable uuid: String,
        request: AllOrderRequest
    ): List<QueryOrderResponse> {
        return userQueryHandler.allOrders(uuid, request)
    }

}