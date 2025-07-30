package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.QueryOrderRequest
import co.nilin.opex.api.ports.proxy.data.TradeRequest
import co.nilin.opex.api.ports.proxy.utils.body
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForObject
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
        origClientOrderId: String?,
    ): Order? {
        return withContext(ProxyDispatchers.market) {
            restTemplate.postForObject<Order?>(
                "$baseUrl/v1/user/${principal.name}/order/query",
                body(QueryOrderRequest(symbol, orderId, origClientOrderId))
            )
        }
    }

    override suspend fun openOrders(principal: Principal, symbol: String?, limit: Int?): List<Order> {
        return withContext(ProxyDispatchers.market) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/${principal.name}/orders/$symbol/open")
                .queryParam("limit", limit ?: 100)
                .build().toUri()
            restTemplate.exchange<Array<Order>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
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
            restTemplate.postForObject<Array<Order>>("$baseUrl/v1/user/${principal.name}/orders", defaultHeaders())
                .toList()
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
            restTemplate.postForObject<Array<Trade>>(
                "$baseUrl/v1/user/${principal.name}/trades",
                body(TradeRequest(symbol, fromTrade, startTime, endTime, limit ?: 500))
            ).toList()
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
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/order/history/$uuid")
                .queryParam("symbol", symbol)
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .queryParam("orderType", orderType)
                .queryParam("direction", direction)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build().toUri()
            restTemplate.exchange<Array<OrderData>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
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
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/order/history/count/$uuid")
                .queryParam("symbol", symbol)
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .queryParam("orderType", orderType)
                .queryParam("direction", direction)
                .build().toUri()
            restTemplate.exchange<Long>(uri, HttpMethod.GET, noBody()).body ?: 0
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
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/trade/history/$uuid")
                .queryParam("symbol", symbol)
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .queryParam("direction", direction)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build().toUri()
            restTemplate.exchange<List<Trade>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
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
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/user/trade/history/count/$uuid")
                .queryParam("symbol", symbol)
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .queryParam("direction", direction)
                .build().toUri()
            restTemplate.exchange<Long>(uri, HttpMethod.GET, noBody()).body ?: 0
        }
    }
}