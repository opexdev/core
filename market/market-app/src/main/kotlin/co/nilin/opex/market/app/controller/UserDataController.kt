package co.nilin.opex.market.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.market.app.utils.asLocalDateTime
import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.UserQueryHandler
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user")
class UserDataController(
    private val userQueryHandler: UserQueryHandler
) {

    @GetMapping("/{uuid}/order/{ouid}")
    suspend fun getOrder(
        @PathVariable uuid: String,
        @PathVariable ouid: String,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Order {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.getOrder(uuid, ouid) ?: throw OpexError.NotFound.exception()
    }

    @PostMapping("/{uuid}/order/query")
    suspend fun queryUserOrder(
        @PathVariable uuid: String, @RequestBody request: QueryOrderRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Order {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.queryOrder(uuid, request) ?: throw OpexError.NotFound.exception()
    }

    @GetMapping("/{uuid}/orders/open")
    suspend fun getUserOpenOrders(
        @PathVariable uuid: String, @RequestParam limit: Int, @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Order> {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.openOrders(uuid, limit)
    }

    @GetMapping("/{uuid}/orders/{symbol}/open")
    suspend fun getUserOpenOrders(
        @PathVariable uuid: String,
        @PathVariable symbol: String,
        @RequestParam limit: Int,
        @CurrentSecurityContext securityContext: SecurityContext,

        ): List<Order> {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.openOrders(uuid, symbol, limit)
    }

    @PostMapping("/{uuid}/orders")
    suspend fun getUserOrders(
        @PathVariable uuid: String,
        @RequestBody request: AllOrderRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Order> {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.allOrders(uuid, request)
    }

    @PostMapping("/{uuid}/trades")
    suspend fun getUserTrades(
        @PathVariable uuid: String,
        @RequestBody request: TradeRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade>? {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.allTrades(uuid, request)
    }

    @PostMapping("/tx/{user}/history")
    suspend fun getTxOfTrades(
        @PathVariable user: String,
        @RequestBody transactionRequest: TransactionRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): TransactionResponse? {
        if (securityContext.authentication.name != user)
            throw OpexError.Forbidden.exception()
        return userQueryHandler.txOfTrades(transactionRequest.apply { owner = user })
    }

    @GetMapping("/order/history")
    suspend fun getOrderHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam orderType: MatchingOrderType?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<OrderData> {
        return userQueryHandler.getOrderHistory(
            securityContext.authentication.name,
            symbol,
            startTime?.let { startTime.asLocalDateTime() },
            endTime?.let { endTime.asLocalDateTime() },
            orderType,
            direction,
            limit,
            offset
        )
    }

    @GetMapping("/order/history/count")
    suspend fun getOrderHistoryCount(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam orderType: MatchingOrderType?,
        @RequestParam direction: OrderDirection?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return userQueryHandler.getOrderHistoryCount(
            securityContext.authentication.name,
            symbol,
            startTime?.let { startTime.asLocalDateTime() },
            endTime?.let { endTime.asLocalDateTime() },
            orderType,
            direction,
        )
    }

    @GetMapping("/trade/history")
    suspend fun getTradeHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade>? {
        return userQueryHandler.getTradeHistory(
            securityContext.authentication.name,
            symbol,
            startTime?.let { startTime.asLocalDateTime() },
            endTime?.let { endTime.asLocalDateTime() },
            direction,
            limit,
            offset
        )
    }

    @GetMapping("/trade/history/count")
    suspend fun getTradeHistoryCount(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam direction: OrderDirection?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {

        return userQueryHandler.getTradeHistoryCount(
            securityContext.authentication.name,
            symbol,
            startTime?.let { startTime.asLocalDateTime() },
            endTime?.let { endTime.asLocalDateTime() },
            direction,
        )
    }

}