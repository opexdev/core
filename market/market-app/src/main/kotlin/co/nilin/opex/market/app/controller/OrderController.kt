package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.AllOrderRequest
import co.nilin.opex.market.core.inout.QueryOrderResponse
import co.nilin.opex.market.core.spi.MarketQueryHandler
import co.nilin.opex.market.core.spi.UserQueryHandler
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/order")
class OrderController(
    private val marketQueryHandler: MarketQueryHandler,
    private val userQueryHandler: UserQueryHandler
) {

    @GetMapping("/{ouid}")
    fun getOrderById(){

    }

    @PostMapping("/user/{uuid}")
    suspend fun getUserOrders(
        @PathVariable uuid: String,
        request: AllOrderRequest
    ): List<QueryOrderResponse> {
        return userQueryHandler.allOrders(uuid, request)
    }

}