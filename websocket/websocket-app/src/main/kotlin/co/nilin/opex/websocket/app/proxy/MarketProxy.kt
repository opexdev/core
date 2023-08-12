package co.nilin.opex.websocket.app.proxy

import co.nilin.opex.websocket.core.inout.*
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*

@Component
class MarketProxy(private val webClient: WebClient) {

    private val logger =LoggerFactory.getLogger(MarketProxy::class.java)

    @Value("\${app.market.url}")
    private lateinit var baseUrl: String

    suspend fun getTradeTickerDataBySymbol(symbol: String, startFrom: Long): PriceChange {
        return webClient.get()
            .uri("$baseUrl/v1/market/$symbol/ticker") {
                it.queryParam("since", startFrom)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<PriceChange>()
            .awaitSingleOrNull()
            ?: PriceChange(symbol, openTime = Date().time, closeTime = startFrom)
    }

    suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBook> {
        return webClient.get()
            .uri("$baseUrl/v1/market/$symbol/order-book") {
                it.queryParam("limit", limit)
                it.queryParam("direction", OrderDirection.BID)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<OrderBook>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBook> {
        return webClient.get()
            .uri("$baseUrl/v1/market/$symbol/order-book") {
                it.queryParam("limit", limit)
                it.queryParam("direction", OrderDirection.ASK)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<OrderBook>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    suspend fun lastOrder(symbol: String): Order? {
        return webClient.get()
            .uri("$baseUrl/v1/market/$symbol/last-order")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Order>()
            .awaitSingleOrNull()
    }

    suspend fun recentTrades(symbol: String, limit: Int): List<MarketTrade> {
        return webClient.get()
            .uri("$baseUrl/v1/market/$symbol/recent-trades") {
                it.queryParam("limit", limit)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<MarketTrade>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    suspend fun lastPrice(symbol: String?): List<PriceTicker> {
        return webClient.get()
            .uri("$baseUrl/v1/market/prices") {
                it.queryParam("symbol", symbol)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<PriceTicker>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    suspend fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData> {
        return webClient.get()
            .uri("$baseUrl/v1/chart/$symbol/candle") {
                it.queryParam("interval", interval)
                it.queryParam("since", startTime)
                it.queryParam("until", endTime)
                it.queryParam("limit", limit)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<CandleData>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }
}