package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderData
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.Trade
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/opex/v1/user/history")
class UserHistoryController(
    private val marketUserDataProxy: MarketUserDataProxy,
) {

    @GetMapping("/order")
    suspend fun getOrderHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam orderType: MatchingOrderType?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int? = 10,
        @RequestParam offset: Int? = 0,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<OrderData> {
        return marketUserDataProxy.getOrderHistory(
            securityContext.authentication.name,
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
            limit,
            offset,
        )
    }

    @GetMapping("/trade")
    suspend fun getOrderHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int? = 10,
        @RequestParam offset: Int? = 0,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade> {
        return marketUserDataProxy.getTradeHistory(
            securityContext.authentication.name, symbol, startTime, endTime, direction, limit, offset
        )
    }
}