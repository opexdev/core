package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.CancelOrderRequest
import co.nilin.opex.api.ports.proxy.data.CreateOrderRequest
import co.nilin.opex.api.ports.proxy.utils.body
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
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
        token: String?,
    ): OrderSubmitResult? {
        logger.info("calling matching-gateway order create")
        val request = CreateOrderRequest(uuid, pair, price, quantity, direction, matchConstraint, orderType, userLevel)
        return restTemplate.postForObject<OrderSubmitResult?>("$baseUrl/order", body(request))
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
            restTemplate.postForObject<OrderSubmitResult?>(
                "$baseUrl/order/cancel",
                body(CancelOrderRequest(ouid, uuid, orderId, symbol))
            )
        }
    }

    override suspend fun getPairSettings(): List<PairSetting> {
        return withContext(ProxyDispatchers.wallet) {
            restTemplate.getForObject<Array<PairSetting>>("$baseUrl/pair-setting", defaultHeaders()).toList()
        }
    }
}