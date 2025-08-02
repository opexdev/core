package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.LoggerDelegate
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

    override fun getTradeTickerData(interval: Interval): List<PriceChange> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/ticker")
            .queryParam("interval", interval)
            .build().toUri()

        return restTemplate.exchange<Array<PriceChange>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getTradeTickerDataBySymbol(symbol: String, interval: Interval): PriceChange {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/ticker")
            .queryParam("interval", interval)
            .build().toUri()

        return restTemplate.exchange<PriceChange?>(uri, HttpMethod.GET, noBody()).body ?: PriceChange(
            symbol,
            openTime = Date().time,
            closeTime = interval.getTime()
        )
    }

    override fun openBidOrders(symbol: String, limit: Int): List<OrderBook> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/order-book")
            .queryParam("limit", limit)
            .queryParam("direction", OrderDirection.BID)
            .build().toUri()

        return restTemplate.exchange<Array<OrderBook>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun openAskOrders(symbol: String, limit: Int): List<OrderBook> {

        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/order-book")
            .queryParam("limit", limit)
            .queryParam("direction", OrderDirection.ASK)
            .build().toUri()

        return restTemplate.exchange<Array<OrderBook>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun lastOrder(symbol: String): Order? {
        return restTemplate.getForObject<Order?>("$baseUrl/v1/market/$symbol/last-order", defaultHeaders())
    }

    override fun recentTrades(symbol: String, limit: Int): List<MarketTrade> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/$symbol/recent-trades")
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<MarketTrade>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun lastPrice(symbol: String?): List<PriceTicker> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/prices")
            .queryParam("symbol", symbol)
            .build().toUri()
        return restTemplate.exchange<Array<PriceTicker>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getBestPriceForSymbols(symbols: List<String>): List<BestPrice> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/best-price")
            .queryParam("symbols", symbols)
            .build().toUri()
        return restTemplate.exchange<Array<BestPrice>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/chart/$symbol/candle")
            .queryParam("interval", interval)
            .queryParam("since", startTime)
            .queryParam("until", endTime)
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<CandleData>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getMarketCurrencyRates(quote: String, base: String?): List<CurrencyRate> {
        val url = if (base.isNullOrEmpty()) "$baseUrl/v1/rate/EXTERNAL" else "$baseUrl/v1/rate/$base/EXTERNAL"
        val uri = UriComponentsBuilder.fromUriString(url)
            .queryParam("quote", quote)
            .build().toUri()
        return restTemplate.exchange<Array<CurrencyRate>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getExternalCurrencyRates(quote: String, base: String?): List<CurrencyRate> {

        val url = if (base.isNullOrEmpty()) "$baseUrl/v1/rate/EXTERNAL" else "$baseUrl/v1/rate/$base/EXTERNAL"
        val uri = UriComponentsBuilder.fromUriString(url)
            .queryParam("quote", quote)
            .build().toUri()
        return restTemplate.exchange<Array<CurrencyRate>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun countActiveUsers(interval: Interval): Long {

        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/active-users")
            .queryParam("interval", interval)
            .build().toUri()
        return restTemplate.exchange<CountResponse?>(uri, HttpMethod.GET, noBody()).body?.value ?: 0
    }

    override fun countTotalOrders(interval: Interval): Long {

        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/orders-count")
            .queryParam("interval", interval)
            .build().toUri()
        return restTemplate.exchange<CountResponse?>(uri, HttpMethod.GET, noBody()).body?.value ?: 0
    }

    override fun countTotalTrades(interval: Interval): Long {

        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/market/trades-count")
            .queryParam("interval", interval)
            .build().toUri()
        return restTemplate.exchange<CountResponse?>(uri, HttpMethod.GET, noBody()).body?.value ?: 0
    }
}