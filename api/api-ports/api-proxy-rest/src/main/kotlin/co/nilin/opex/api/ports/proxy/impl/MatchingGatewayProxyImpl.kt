package co.nilin.opex.api.ports.proxy.impl


import co.nilin.opex.api.core.inout.OrderSubmitResult
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.core.utils.LoggerDelegate
import co.nilin.opex.api.ports.proxy.data.CancelOrderRequest
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

@Component
class MatchingGatewayProxyImpl(private val client: WebClient) : MatchingGatewayProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.matching-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun createNewOrder(
        order: MatchingGatewayProxy.CreateOrderRequest,
        token: String?
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order create")
        return client.post()
            .uri(URI.create("$baseUrl/order"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .body(Mono.just(order))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<OrderSubmitResult>()
            .awaitSingleOrNull()
    }

    override suspend fun cancelOrder(
        ouid: String,
        uuid: String,
        orderId: Long,
        symbol: String,
        token: String?
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order cancel")
        return client.post()
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