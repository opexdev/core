package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderData
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.*

@RestController()
@RequestMapping("/opex/v1/user/history")
class UserHistoryController(private val marketUserDataProxy: MarketUserDataProxy) {

    @GetMapping("/order")
    suspend fun getOrderHistory(
        @RequestParam symbol: String?,
        @RequestParam fromDate: Date?,
        @RequestParam toDate: Date?,
        @RequestParam orderType: MatchingOrderType?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        principal: Principal,
    ): List<OrderData> {
        return marketUserDataProxy.getOrderHistory(principal, symbol, fromDate, toDate, orderType, direction, limit, offset,)
    }

}