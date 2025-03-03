package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.gateway.app.inout.CancelOrderRequest
import co.nilin.opex.matching.gateway.app.inout.CreateOrderRequest
import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderCancelRequestEvent
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitRequestEvent
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitResult
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.KafkaHealthIndicator
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.OrderRequestEventSubmitter
import co.nilin.opex.matching.gateway.ports.postgres.service.PairSettingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class OrderService(
    val accountantApiProxy: AccountantApiProxy,
    val orderRequestEventSubmitter: OrderRequestEventSubmitter,
    val pairConfigLoader: PairConfigLoader,
    val pairSettingService: PairSettingService,
    private val kafkaHealthIndicator: KafkaHealthIndicator,
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    suspend fun submitNewOrder(createOrderRequest: CreateOrderRequest): OrderSubmitResult {
        require(createOrderRequest.price >= BigDecimal.ZERO)

        val pairSetting = pairSettingService.load(createOrderRequest.pair)
        if (!pairSetting.isAvailable)
            throw OpexError.PairIsNotAvailable.exception()

        val symbolSides = createOrderRequest.pair.split("_")
        val symbol = if (createOrderRequest.direction == OrderDirection.ASK)
            symbolSides[0]
        else
            symbolSides[1]

        val pairConfig = pairConfigLoader.load(createOrderRequest.pair, createOrderRequest.direction)

        val canCreateOrder = runCatching {
            accountantApiProxy.canCreateOrder(
                createOrderRequest.uuid!!,
                symbol,
                if (createOrderRequest.direction == OrderDirection.ASK)
                    createOrderRequest.quantity
                else
                    createOrderRequest.quantity.multiply(createOrderRequest.price)
            )
        }.onFailure { logger.error(it.message) }.getOrElse { false }

        if (!canCreateOrder)
            throw OpexError.SubmitOrderForbiddenByAccountant.exception()

        if (!kafkaHealthIndicator.isHealthy)
            throw OpexError.ServiceUnavailable.exception()

        val orderSubmitRequest = OrderSubmitRequestEvent(
            createOrderRequest.uuid!!, //get from auth2
            Pair(symbolSides[0], symbolSides[1]),
            createOrderRequest.price
                .divide(pairConfig.rightSideFraction)
                .longValueExact(),
            createOrderRequest.quantity
                .divide(pairConfig.leftSideFraction)
                .longValueExact(),
            createOrderRequest.direction,
            createOrderRequest.matchConstraint,
            createOrderRequest.orderType,
            createOrderRequest.userLevel
        )
        return orderRequestEventSubmitter.submit(orderSubmitRequest)
    }

    suspend fun cancelOrder(request: CancelOrderRequest): OrderSubmitResult {
        val symbols = request.symbol.split("_")
        val event = OrderCancelRequestEvent(request.ouid, request.uuid, Pair(symbols[0], symbols[1]), request.orderId)
        return orderRequestEventSubmitter.submit(event)
    }
}
