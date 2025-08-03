package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.common.utils.Interval

interface MarketDataProxy {

    fun getTradeTickerData(interval: Interval): List<PriceChange>

    fun getTradeTickerDataBySymbol(symbol: String, interval: Interval): PriceChange

    fun openBidOrders(symbol: String, limit: Int): List<OrderBook>

    fun openAskOrders(symbol: String, limit: Int): List<OrderBook>

    fun lastOrder(symbol: String): Order?

    fun recentTrades(symbol: String, limit: Int): List<MarketTrade>

    fun lastPrice(symbol: String?): List<PriceTicker>

    fun getBestPriceForSymbols(symbols: List<String>): List<BestPrice>

    fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData>

    fun getMarketCurrencyRates(quote: String, base: String? = null): List<CurrencyRate>

    fun getExternalCurrencyRates(quote: String, base: String? = null): List<CurrencyRate>

    fun countActiveUsers(interval: Interval): Long

    fun countTotalOrders(interval: Interval): Long

    fun countTotalTrades(interval: Interval): Long

}