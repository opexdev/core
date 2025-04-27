package co.nilin.opex.market.app.controller

import co.nilin.opex.market.app.data.RecentTradesRequest
import co.nilin.opex.market.app.utils.asLocalDateTime
import co.nilin.opex.market.core.inout.TradeData
import co.nilin.opex.market.core.spi.MarketQueryHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin")
class AdminController(private val marketQueryHandler: MarketQueryHandler) {

    @PostMapping("/recent-trades")
    suspend fun getRecentTrades(
        @RequestBody request: RecentTradesRequest,
    ): List<TradeData> {
        return marketQueryHandler.recentTrades(
            request.symbol,
            request.makerUuid,
            request.takerUuid,
            request.fromDate?.asLocalDateTime(),
            request.toDate?.asLocalDateTime(),
            request.excludeSelfTrade,
            request.limit,
            request.offset
        )
    }
}