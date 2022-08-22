package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.UserQueryHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user")
class UserDataController(private val userQueryHandler: UserQueryHandler) {

    @GetMapping("/{uuid}/order/{ouid}")
    suspend fun getOrder(@PathVariable uuid: String, @PathVariable ouid: String): Order {
        return userQueryHandler.getOrder(uuid, ouid) ?: throw OpexException(OpexError.NotFound)
    }

    @PostMapping("/{uuid}/order/query")
    suspend fun queryUserOrder(@PathVariable uuid: String, @RequestBody request: QueryOrderRequest): Order {
        return userQueryHandler.queryOrder(uuid, request) ?: throw OpexException(OpexError.NotFound)
    }

    @GetMapping("/{uuid}/orders/{symbol}/open")
    suspend fun getUserOpenOrders(
        @PathVariable uuid: String,
        @PathVariable symbol: String,
        @RequestParam limit: Int
    ): List<Order> {
        return userQueryHandler.openOrders(uuid, symbol, limit)
    }

    @PostMapping("/{uuid}/orders")
    suspend fun getUserOrders(@PathVariable uuid: String, @RequestBody request: AllOrderRequest): List<Order> {
        return userQueryHandler.allOrders(uuid, request)
    }

    @PostMapping("/{uuid}/trades")
    suspend fun getUserTrades(@PathVariable uuid: String, @RequestBody request: TradeRequest): List<Trade> {
        return userQueryHandler.allTrades(uuid, request)
    }

}