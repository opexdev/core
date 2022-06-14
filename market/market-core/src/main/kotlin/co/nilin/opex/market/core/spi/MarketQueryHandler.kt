package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.*
import java.time.LocalDateTime

interface MarketQueryHandler {

    suspend fun getTradeTickerData(startFrom: LocalDateTime): List<PriceChange>

    suspend fun getTradeTickerDateBySymbol(symbol: String, startFrom: LocalDateTime): PriceChange?

    suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBook>

    suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBook>

    suspend fun lastOrder(symbol: String): Order?

    suspend fun recentTrades(symbol: String, limit: Int): List<MarketTrade>

    suspend fun lastPrice(symbol: String?): List<PriceTickerResponse>

    suspend fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData>

}