package co.nilin.mixchange.api.core.spi

import co.nilin.mixchange.api.core.inout.*
import kotlinx.coroutines.flow.Flow
import java.security.Principal

interface UserQueryHandler {
    suspend fun queryOrder(principal: Principal, request: QueryOrderRequest): QueryOrderResponse?
    suspend fun openOrders(principal: Principal, symbol: String?): Flow<QueryOrderResponse>
    suspend fun allOrders(principal: Principal, allOrderRequest: AllOrderRequest): Flow<QueryOrderResponse>
    suspend fun allTrades(principal: Principal, request: TradeRequest): Flow<TradeResponse>
}