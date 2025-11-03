package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.CancelOrderRequest
import co.nilin.opex.api.ports.proxy.data.CreateOrderRequest
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
import java.math.BigDecimal
import java.net.URI

@Component
class MatchingGatewayProxyImpl(@Qualifier("generalWebClient") private val client: WebClient) : MatchingGatewayProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.matching-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun createNewOrder(
        uuid: String?,
        pair: String,
        price: BigDecimal,
        quantity: BigDecimal,
        direction: OrderDirection,
        matchConstraint: MatchConstraint?,
        orderType: MatchingOrderType,
        userLevel: String,
        token: String?,
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order create")
        val body = CreateOrderRequest(uuid, pair, price, quantity, direction, matchConstraint, orderType, userLevel)
        return withContext(ProxyDispatchers.general) {
            client.post()
                .uri(URI.create("$baseUrl/order"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .body(Mono.just(body))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<OrderSubmitResult>()
                .awaitSingleOrNull()
        }

    }

    override suspend fun cancelOrder(
        ouid: String,
        uuid: String,
        orderId: Long,
        symbol: String,
        token: String?,
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order cancel")
        return withContext(ProxyDispatchers.general) {
            client.post()
                .uri(URI.create("$baseUrl/order/cancel"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .body(Mono.just(CancelOrderRequest(ouid, uuid, orderId, symbol)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<OrderSubmitResult>()
                .awaitSingleOrNull()
        }
    }

    override suspend fun getPairSettings(): List<PairSetting> {
        return withContext(ProxyDispatchers.wallet) {
            client.get()
                .uri("$baseUrl/pair-setting")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<PairSetting>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }
}