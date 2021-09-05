package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MarketQueryHandler {

    suspend fun getTradeTickerData(startFrom: LocalDateTime): List<PriceChangeResponse>

    suspend fun getTradeTickerDataBySymbol(symbol: String, startFrom: LocalDateTime): PriceChangeResponse

    suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBookResponse>

    suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBookResponse>

    suspend fun lastOrder(symbol: String): QueryOrderResponse?

    suspend fun recentTrades(symbol: String, limit: Int): Flow<MarketTradeResponse>

}