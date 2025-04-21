package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Component
class MarketDataProxyImpl(private val restTemplate: RestTemplate) : MarketDataProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.market.url}")
    private lateinit var baseUrl: String

    override suspend fun getTradeTickerData(interval: Interval): List<PriceChange> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/ticker")
                .queryParam("interval", interval)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<PriceChange>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getTradeTickerDataBySymbol(symbol: String, interval: Interval): PriceChange {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/ticker")
                .queryParam("interval", interval)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                PriceChange::class.java
            ).body ?: PriceChange(symbol, openTime = Date().time, closeTime = interval.getTime())
        }
    }

    override suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBook> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/order-book")
                .queryParam("limit", limit)
                .queryParam("direction", OrderDirection.BID)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<OrderBook>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBook> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/order-book")
                .queryParam("limit", limit)
                .queryParam("direction", OrderDirection.ASK)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<OrderBook>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun lastOrder(symbol: String): Order? {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/last-order")
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Order::class.java
            ).body
        }
    }

    override suspend fun recentTrades(symbol: String, limit: Int): List<MarketTrade> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/recent-trades")
                .queryParam("limit", limit)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<MarketTrade>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun lastPrice(symbol: String?): List<PriceTicker> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/prices")
                .queryParam("symbol", symbol)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<PriceTicker>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getBestPriceForSymbols(symbols: List<String>): List<BestPrice> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/best-prices")
                .queryParam("symbols", symbols)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<BestPrice>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/chart/$symbol/candle")
                .queryParam("interval", interval)
                .queryParam("since", startTime)
                .queryParam("until", endTime)
                .queryParam("limit", limit)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<CandleData>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getMarketCurrencyRates(quote: String, base: String?): List<CurrencyRate> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString(
                if (base.isNullOrEmpty()) "$baseUrl/v1/rate/EXTERNAL" else "$baseUrl/v1/rate/$base/EXTERNAL"
            )
                .queryParam("quote", quote)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<CurrencyRate>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getExternalCurrencyRates(quote: String, base: String?): List<CurrencyRate> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString(
                if (base.isNullOrEmpty()) "$baseUrl/v1/rate/EXTERNAL" else "$baseUrl/v1/rate/$base/EXTERNAL"
            )
                .queryParam("quote", quote)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<CurrencyRate>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun countActiveUsers(interval: Interval): Long {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/active-users")
                .queryParam("interval", interval)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                CountResponse::class.java
            ).body?.value ?: 0
        }
    }

    override suspend fun countTotalOrders(interval: Interval): Long {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/orders-count")
                .queryParam("interval", interval)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                CountResponse::class.java
            ).body?.value ?: 0
        }
    }

    override suspend fun countTotalTrades(interval: Interval): Long {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/trades-count")
                .queryParam("interval", interval)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                CountResponse::class.java
            ).body?.value ?: 0
        }
    }
}