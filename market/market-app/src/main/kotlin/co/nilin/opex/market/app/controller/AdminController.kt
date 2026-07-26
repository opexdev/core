package co.nilin.opex.market.app.controller

import co.nilin.opex.market.app.data.AdminOrdersHistoryRequest
import co.nilin.opex.market.app.data.RecentTradesRequest
import co.nilin.opex.market.app.data.AdminTradesHistoryRequest
import co.nilin.opex.market.app.utils.asLocalDateTime
import co.nilin.opex.market.core.inout.OrderData
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
    ): List<TradeData>? {
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

    @PostMapping("/trades/history")
    suspend fun searchTradesAdmin(
        @RequestBody request: AdminTradesHistoryRequest,
    ): List<TradeData>? {
        return marketQueryHandler.recentTradesAdmin(
            symbol=request.symbol,
            baseAsset = request.baseAsset,
            quoteAsset = request.quoteAsset,
            uuid = request.uuid,
            makerUuid = request.makerUuid,
            takerUuid = request.takerUuid,
            ouid = request.ouid,
            makerOuid = request.makerOuid,
            takerOuid = request.takerOuid,
            fromDate = request.fromDate?.asLocalDateTime(),
            toDate = request.toDate?.asLocalDateTime(),
            excludeSelfTrade = request.excludeSelfTrade,
            ascendingByTime = request.ascendingByTime,
            limit = request.limit,
            offset = request.offset
        )
    }
    @PostMapping("/orders/history")
    suspend fun searchOrdersAdmin(
        @RequestBody request: AdminOrdersHistoryRequest,
    ): List<OrderData>? {
        return marketQueryHandler.recentOrdersAdmin(
           request.uuid,
            request.symbol,
            request.ouid,
            request.startTime?.asLocalDateTime(),
            request.endTime?.asLocalDateTime(),
            request.orderType,
            request.direction,
            request.ascendingByTime,
            request.limit,
            request.offset
        )
    }
}