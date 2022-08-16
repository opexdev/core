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

    suspend fun lastPrice(symbol: String?): List<PriceTicker>

    suspend fun getBestPriceForSymbols(symbols: List<String>): List<BestPrice>

    suspend fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData>

    suspend fun numberOfActiveUsers(interval: LocalDateTime): Long

    suspend fun numberOfTrades(interval: LocalDateTime, pair: String? = null): Long

    suspend fun numberOfOrders(interval: LocalDateTime, pair: String? = null): Long

    suspend fun mostIncreasePrice(interval: LocalDateTime, limit: Int): List<PriceStat>

    suspend fun mostDecreasePrice(interval: LocalDateTime, limit: Int): List<PriceStat>

    suspend fun mostVolume(interval: LocalDateTime): TradeVolumeStat?

    suspend fun mostTrades(interval: LocalDateTime): TradeVolumeStat?

}