package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.getForObject
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
                .build().toUri()

            restTemplate.exchange<Array<PriceChange>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getTradeTickerDataBySymbol(symbol: String, interval: Interval): PriceChange {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/ticker")
                .queryParam("interval", interval)
                .build().toUri()

            restTemplate.exchange<PriceChange?>(uri, HttpMethod.GET, noBody()).body ?: PriceChange(
                symbol,
                openTime = Date().time,
                closeTime = interval.getTime()
            )
        }
    }

    override suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBook> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/order-book")
                .queryParam("limit", limit)
                .queryParam("direction", OrderDirection.BID)
                .build().toUri()

            restTemplate.exchange<Array<OrderBook>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBook> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/order-book")
                .queryParam("limit", limit)
                .queryParam("direction", OrderDirection.ASK)
                .build().toUri()

            restTemplate.exchange<Array<OrderBook>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun lastOrder(symbol: String): Order? {
        return withContext(ProxyDispatchers.market) {
            restTemplate.getForObject<Order?>("$baseUrl/v1/market/$symbol/last-order", defaultHeaders())
        }
    }

    override suspend fun recentTrades(symbol: String, limit: Int): List<MarketTrade> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/recent-trades")
                .queryParam("limit", limit)
                .build().toUri()
            restTemplate.exchange<Array<MarketTrade>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun lastPrice(symbol: String?): List<PriceTicker> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/prices")
                .queryParam("symbol", symbol)
                .build().toUri()
            restTemplate.exchange<Array<PriceTicker>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getBestPriceForSymbols(symbols: List<String>): List<BestPrice> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/best-price")
                .queryParam("symbols", symbols)
                .build().toUri()
            restTemplate.exchange<Array<BestPrice>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
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
                .build().toUri()
            restTemplate.exchange<Array<CandleData>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getMarketCurrencyRates(quote: String, base: String?): List<CurrencyRate> {
        return withContext(ProxyDispatchers.market) {
            val url = if (base.isNullOrEmpty()) "$baseUrl/v1/rate/EXTERNAL" else "$baseUrl/v1/rate/$base/EXTERNAL"
            val uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("quote", quote)
                .build().toUri()
            restTemplate.exchange<Array<CurrencyRate>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getExternalCurrencyRates(quote: String, base: String?): List<CurrencyRate> {
        return withContext(ProxyDispatchers.market) {
            val url = if (base.isNullOrEmpty()) "$baseUrl/v1/rate/EXTERNAL" else "$baseUrl/v1/rate/$base/EXTERNAL"
            val uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("quote", quote)
                .build().toUri()
            restTemplate.exchange<Array<CurrencyRate>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
        }
    }

    override suspend fun countActiveUsers(interval: Interval): Long {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/active-users")
                .queryParam("interval", interval)
                .build().toUri()
            restTemplate.exchange<CountResponse?>(uri, HttpMethod.GET, noBody()).body?.value ?: 0
        }
    }

    override suspend fun countTotalOrders(interval: Interval): Long {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/orders-count")
                .queryParam("interval", interval)
                .build().toUri()
            restTemplate.exchange<CountResponse?>(uri, HttpMethod.GET, noBody()).body?.value ?: 0
        }
    }

    override suspend fun countTotalTrades(interval: Interval): Long {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/trades-count")
                .queryParam("interval", interval)
                .build().toUri()
            restTemplate.exchange<CountResponse?>(uri, HttpMethod.GET, noBody()).body?.value ?: 0
        }
    }
}