package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.QueryOrderResponse
import co.nilin.opex.api.core.inout.TradeResponse
import kotlinx.coroutines.flow.Flow
import java.security.Principal

interface MarketQueryHandler {

    suspend fun openBidOrders(symbol: String, limit: Int): List<QueryOrderResponse>

    suspend fun openAskOrders(symbol: String, limit: Int): List<QueryOrderResponse>

    suspend fun recentTrades(principal: Principal, symbol: String, limit: Int): Flow<TradeResponse>

}