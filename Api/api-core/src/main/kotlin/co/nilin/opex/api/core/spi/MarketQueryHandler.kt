package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.MarketTradeResponse
import co.nilin.opex.api.core.inout.QueryOrderResponse
import kotlinx.coroutines.flow.Flow
import java.security.Principal

interface MarketQueryHandler {

    suspend fun openBidOrders(symbol: String, limit: Int): List<QueryOrderResponse>

    suspend fun openAskOrders(symbol: String, limit: Int): List<QueryOrderResponse>

    suspend fun lastOrder(symbol: String): QueryOrderResponse?

    suspend fun recentTrades(principal: Principal, symbol: String, limit: Int): Flow<MarketTradeResponse>

}