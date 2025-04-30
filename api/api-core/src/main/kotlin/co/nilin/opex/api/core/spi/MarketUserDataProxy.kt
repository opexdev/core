package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.Order
import co.nilin.opex.api.core.inout.OrderData
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.Trade
import java.security.Principal
import java.util.*

interface MarketUserDataProxy {

    suspend fun queryOrder(principal: Principal, symbol: String, orderId: Long?, origClientOrderId: String?): Order?

    suspend fun openOrders(principal: Principal, symbol: String?, limit: Int?): List<Order>

    suspend fun allOrders(
        principal: Principal,
        symbol: String?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Order>

    suspend fun allTrades(
        principal: Principal,
        symbol: String?,
        fromTrade: Long?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Trade>

    suspend fun getOrderHistory(
        uuid : String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<OrderData>

    suspend fun getTradeHistory(
        uuid : String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<Trade>
}