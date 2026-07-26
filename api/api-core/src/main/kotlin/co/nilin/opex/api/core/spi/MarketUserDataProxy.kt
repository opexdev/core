package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.Order
import co.nilin.opex.api.core.inout.OrderData
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.Trade
import java.security.Principal
import java.util.*

interface MarketUserDataProxy {

    suspend fun queryOrder(token: String, symbol: String, orderId: Long?, origClientOrderId: String?): Order?

    suspend fun openOrders(token: String, symbol: String?, limit: Int?): List<Order>

    suspend fun allOrders(
        token: String,
        symbol: String?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Order>

    suspend fun allTrades(
        token: String,
        symbol: String?,
        fromTrade: Long?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Trade>

    suspend fun getOrderHistory(
        token: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<OrderData>

    suspend fun getOrderHistoryCount(
        token: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
    ): Long

    suspend fun getTradeHistory(
        token: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<Trade>

    suspend fun getTradeHistoryCount(
        token: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        direction: OrderDirection?,
    ): Long
}