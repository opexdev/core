package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import java.time.LocalDateTime

interface MarketDataProxy {

    suspend fun getTradeTickerData(startFrom: Long): List<PriceChange>

    suspend fun getTradeTickerDataBySymbol(symbol: String, startFrom: Long): PriceChange

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

    suspend fun getMarketCurrencyRates(quote: String, base: String? = null): List<CurrencyRate>

    suspend fun getExternalCurrencyRates(quote: String, base: String? = null): List<CurrencyRate>

    suspend fun countActiveUsers(since: Long): Long

    suspend fun countTotalOrders(since: Long): Long

    suspend fun countTotalTrades(since: Long): Long

}