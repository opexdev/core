package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.MatchConstraint
import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderSubmitResult
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.CancelOrderRequest
import co.nilin.opex.api.ports.proxy.data.CreateOrderRequest
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal

@Component
class MatchingGatewayProxyImpl(private val restTemplate: RestTemplate) : MatchingGatewayProxy {

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
        token: String?
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order create")
        val body = CreateOrderRequest(uuid, pair, price, quantity, direction, matchConstraint, orderType, userLevel)
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/order")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer $token")
                .body(body)

            restTemplate.exchange(
                request,
                OrderSubmitResult::class.java
            ).body
        }

    }

    override suspend fun cancelOrder(
        ouid: String,
        uuid: String,
        orderId: Long,
        symbol: String,
        token: String?
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order cancel")
        return withContext(ProxyDispatchers.general) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/order/cancel")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer $token")
                .body(CancelOrderRequest(ouid, uuid, orderId, symbol))

            restTemplate.exchange(
                request,
                OrderSubmitResult::class.java
            ).body
        }
    }
}