package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.*

interface UserQueryHandler {

    suspend fun getOrder(uuid: String, ouid: String): Order?

    suspend fun queryOrder(uuid: String, request: QueryOrderRequest): Order?

    suspend fun openOrders(uuid: String, symbol: String?, limit: Int): List<Order>

    suspend fun allOrders(uuid: String, allOrderRequest: AllOrderRequest): List<Order>

    suspend fun allTrades(uuid: String, request: TradeRequest): List<Trade>
}