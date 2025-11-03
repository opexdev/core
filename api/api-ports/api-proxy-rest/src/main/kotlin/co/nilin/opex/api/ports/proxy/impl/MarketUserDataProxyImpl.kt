package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.AllOrderRequest
import co.nilin.opex.api.ports.proxy.data.QueryOrderRequest
import co.nilin.opex.api.ports.proxy.data.TradeRequest
import co.nilin.opex.common.utils.LoggerDelegate
import io.netty.handler.timeout.TimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@Component
class MarketUserDataProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : MarketUserDataProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.market.url}")
    private lateinit var baseUrl: String
    private suspend fun <T> retryOnce(backoffMs: Long = 200, block: suspend () -> T): T =
        try {
            block()
        } catch (e: TimeoutException) {
            delay(backoffMs); block()
        }

    private val mgLimiter = Semaphore(permits = 16, acquiredPermits = 0) // fair-like behavior

    override suspend fun queryOrder(
        principal: Principal,
        symbol: String,
        orderId: Long?,
        origClientOrderId: String?,
    ): Order? {
        return withContext(ProxyDispatchers.market) {
            webClient.post()
                .uri("$baseUrl/v1/user/${principal.name}/order/query")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(QueryOrderRequest(symbol, orderId, origClientOrderId)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Order>()
                .awaitSingleOrNull()
        }
    }

    override suspend fun openOrders(principal: Principal, symbol: String?, limit: Int?): List<Order> {
        return withContext(ProxyDispatchers.market) {
            mgLimiter.withPermit {
                retryOnce {
                    webClient.get()
                        .uri("$baseUrl/v1/user/${principal.name}/orders/$symbol/open") {
                            it.queryParam("limit", limit ?: 100)
                            it.build()
                        }.accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .retrieve()
                        .onStatus({ t -> t.isError }, { it.createException() })
                        .bodyToFlux<Order>()
                        .collectList()
                        .awaitFirstOrElse { emptyList() }
                }
            }
        }
    }

    override suspend fun allOrders(
        principal: Principal,
        symbol: String?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?,
    ): List<Order> {
        return withContext(ProxyDispatchers.market) {
            webClient.post()
                .uri("$baseUrl/v1/user/${principal.name}/orders")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(AllOrderRequest(symbol, startTime, endTime, limit ?: 500)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<Order>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun allTrades(
        principal: Principal,
        symbol: String?,
        fromTrade: Long?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?,
    ): List<Trade> {
        return withContext(ProxyDispatchers.market) {
            webClient.post()
                .uri("$baseUrl/v1/user/${principal.name}/trades")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(TradeRequest(symbol, fromTrade, startTime, endTime, limit ?: 500)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<Trade>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getOrderHistory(
        uuid: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<OrderData> {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/user/order/history/$uuid") {
                    it.queryParam("symbol", symbol)
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("orderType", orderType)
                    it.queryParam("direction", direction)
                    it.queryParam("limit", limit)
                    it.queryParam("offset", offset)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<OrderData>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getOrderHistoryCount(
        uuid: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
    ): Long {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/user/order/history/count/$uuid") {
                    it.queryParam("symbol", symbol)
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("orderType", orderType)
                    it.queryParam("direction", direction)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Long>()
                .awaitFirstOrElse { 0L }
        }
    }

    override suspend fun getTradeHistory(
        uuid: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<Trade> {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/user/trade/history/$uuid") {
                    it.queryParam("symbol", symbol)
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("direction", direction)
                    it.queryParam("limit", limit)
                    it.queryParam("offset", offset)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<Trade>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getTradeHistoryCount(
        uuid: String,
        symbol: String?,
        startTime: Long?,
        endTime: Long?,
        direction: OrderDirection?,
    ): Long {
        return withContext(ProxyDispatchers.market) {
            webClient.get()
                .uri("$baseUrl/v1/user/trade/history/count/$uuid") {
                    it.queryParam("symbol", symbol)
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("direction", direction)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Long>()
                .awaitFirstOrElse { 0L }
        }
    }
}