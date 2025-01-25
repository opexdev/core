package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.common.utils.Interval

interface MarketDataProxy {

    suspend fun getTradeTickerData(interval: Interval): List<PriceChange>

    suspend fun getTradeTickerDataBySymbol(symbol: String, interval: Interval): PriceChange

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

    suspend fun countActiveUsers(interval: Interval): Long

    suspend fun countTotalOrders(interval: Interval): Long

    suspend fun countTotalTrades(interval: Interval): Long

}