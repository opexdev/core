package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.Order
import co.nilin.opex.api.core.inout.Trade
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.AllOrderRequest
import co.nilin.opex.api.ports.proxy.data.QueryOrderRequest
import co.nilin.opex.api.ports.proxy.data.TradeRequest
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal
import java.util.*

@Component
class MarketUserDataProxyImpl(private val restTemplate: RestTemplate) : MarketUserDataProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.market.url}")
    private lateinit var baseUrl: String

    override suspend fun queryOrder(
        principal: Principal,
        symbol: String,
        orderId: Long?,
        origClientOrderId: String?
    ): Order? {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/${principal.name}/order/query")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(QueryOrderRequest(symbol, orderId, origClientOrderId))

            restTemplate.exchange(
                request,
                Order::class.java
            ).body
        }
    }

    override suspend fun openOrders(principal: Principal, symbol: String?, limit: Int?): List<Order> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/${principal.name}/orders/$symbol/open")
                .queryParam("limit", limit ?: 100)
                .build()
                .toUri()

            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<Order>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun allOrders(
        principal: Principal,
        symbol: String?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Order> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/${principal.name}/orders")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(AllOrderRequest(symbol, startTime, endTime, limit ?: 500))

            restTemplate.exchange(
                request,
                Array<Order>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun allTrades(
        principal: Principal,
        symbol: String?,
        fromTrade: Long?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Trade> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/${principal.name}/trades")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(TradeRequest(symbol, fromTrade, startTime, endTime, limit ?: 500))

            restTemplate.exchange(
                request,
                Array<Trade>::class.java
            ).body?.toList() ?: emptyList()
        }
    }
}