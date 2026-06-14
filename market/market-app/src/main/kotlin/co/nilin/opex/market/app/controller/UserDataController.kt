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

    @GetMapping("/order/{ouid}")
    suspend fun getOrder(
        @PathVariable ouid: String,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Order {
        return userQueryHandler.getOrder(securityContext.authentication.name, ouid)
            ?: throw OpexError.NotFound.exception()
    }

    @PostMapping("/order/query")
    suspend fun queryUserOrder(
        @RequestBody request: QueryOrderRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Order {
        return userQueryHandler.queryOrder(securityContext.authentication.name, request)
            ?: throw OpexError.NotFound.exception()
    }

    @GetMapping("/orders/open")
    suspend fun getUserOpenOrders(
        @RequestParam limit: Int, @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Order> {
        return userQueryHandler.openOrders(securityContext.authentication.name, limit)
    }

    @GetMapping("/orders/{symbol}/open")
    suspend fun getUserOpenOrders(
        @PathVariable symbol: String,
        @RequestParam limit: Int,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Order> {
        return userQueryHandler.openOrders(securityContext.authentication.name, symbol, limit)
    }

    @PostMapping("/orders")
    suspend fun getUserOrders(
        @RequestBody request: AllOrderRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Order> {
        return userQueryHandler.allOrders(securityContext.authentication.name, request)
    }

    @PostMapping("/trades")
    suspend fun getUserTrades(
        @RequestBody request: TradeRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade>? {
        return userQueryHandler.allTrades(securityContext.authentication.name, request)
    }

    @PostMapping("/tx/history")
    suspend fun getTxOfTrades(
        @RequestBody transactionRequest: TransactionRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): TransactionResponse? {
        return userQueryHandler.txOfTrades(transactionRequest.apply { owner = securityContext.authentication.name })
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