package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.Order
import co.nilin.opex.api.core.inout.Trade
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.core.utils.LoggerDelegate
import co.nilin.opex.api.ports.proxy.data.AllOrderRequest
import co.nilin.opex.api.ports.proxy.data.QueryOrderRequest
import co.nilin.opex.api.ports.proxy.data.TradeRequest
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
class MarketUserDataProxyImpl(private val webClient: WebClient) : MarketUserDataProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.market.url}")
    private lateinit var baseUrl: String

    override suspend fun queryOrder(
        principal: Principal,
        symbol: String,
        orderId: Long?,
        origClientOrderId: String?
    ): Order? {
        return webClient.post()
            .uri("$baseUrl/v1/user/${principal.name}/order/query")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(QueryOrderRequest(symbol, orderId, origClientOrderId)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Order>()
            .awaitSingleOrNull()
    }

    override suspend fun openOrders(principal: Principal, symbol: String?): List<Order> {
        return webClient.get()
            .uri("$baseUrl/v1/user/${principal.name}/orders/$symbol/open")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<Order>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun allOrders(
        principal: Principal,
        symbol: String?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Order> {
        return webClient.post()
            .uri("$baseUrl/v1/user/${principal.name}/orders")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(AllOrderRequest(symbol, startTime, endTime, limit)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<Order>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun allTrades(
        principal: Principal,
        symbol: String?,
        fromTrade: Long?,
        startTime: Date?,
        endTime: Date?,
        limit: Int?
    ): List<Trade> {
        return webClient.post()
            .uri("$baseUrl/v1/user/${principal.name}/trades")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(TradeRequest(symbol, fromTrade, startTime, endTime, limit)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<Trade>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

}