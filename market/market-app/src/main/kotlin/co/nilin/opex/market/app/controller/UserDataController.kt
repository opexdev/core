package co.nilin.opex.market.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.UserQueryHandler
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/user")
class UserDataController(private val userQueryHandler: UserQueryHandler) {

    @GetMapping("/{uuid}/order/{ouid}")
    suspend fun getOrder(@PathVariable uuid: String, @PathVariable ouid: String): Order {
        return userQueryHandler.getOrder(uuid, ouid) ?: throw OpexError.NotFound.exception()
    }

    @PostMapping("/{uuid}/order/query")
    suspend fun queryUserOrder(@PathVariable uuid: String, @RequestBody request: QueryOrderRequest): Order {
        return userQueryHandler.queryOrder(uuid, request) ?: throw OpexError.NotFound.exception()
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

    @PostMapping("/tx/{user}/history")
    suspend fun getTxOfTrades(@PathVariable user: String,
                              @RequestBody transactionRequest: TransactionRequest,
                              @CurrentSecurityContext securityContext: SecurityContext): TxOfTrades? {
        if (securityContext.authentication.name != user)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.txOfTrades(transactionRequest.apply { owner = user })
    }

}