package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.*
import kotlinx.coroutines.flow.Flow
import java.security.Principal

interface UserQueryHandler {

    suspend fun queryOrder(uuid: String, request: QueryOrderRequest): QueryOrderResponse?

    suspend fun openOrders(uuid: String, symbol: String?): List<QueryOrderResponse>

    suspend fun allOrders(uuid: String, allOrderRequest: AllOrderRequest): List<QueryOrderResponse>

    suspend fun allTrades(uuid: String, request: TradeRequest): List<TradeResponse>
}