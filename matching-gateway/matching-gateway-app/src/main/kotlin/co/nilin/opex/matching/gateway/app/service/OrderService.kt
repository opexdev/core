package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.engine.core.model.OrderType
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
        // Replace the validation block in submitNewOrder
        require(
            (createOrderRequest.matchConstraint == MatchConstraint.IOC_BUDGET &&
                    createOrderRequest.totalBudget != null) ||
                    (createOrderRequest.matchConstraint != MatchConstraint.IOC_BUDGET &&
                            createOrderRequest.price!! >= BigDecimal.ZERO)
        )

        val pairSetting = pairSettingService.load(createOrderRequest.pair)
        if (!pairSetting.isAvailable)
            throw OpexError.PairIsNotAvailable.exception()
        if (!pairSetting.orderTypes.split(",").contains(createOrderRequest.orderType.name)) {
            throw OpexError.InvalidOrderType.exception()
        }

        if (createOrderRequest.matchConstraint == MatchConstraint.IOC_BUDGET) {
            requireNotNull(createOrderRequest.totalBudget) { "Total budget required for IOC_BUDGET" }

            // Different validation for ASK vs BID orders
            if (createOrderRequest.direction == OrderDirection.ASK) {
                // For ASK orders, price should be null for market orders
                if (createOrderRequest.orderType == OrderType.MARKET_ORDER) {
                    require(createOrderRequest.price == null) { "Price must be null for market ASK IOC_BUDGET orders" }
                } else {
                    // For limit ASK orders, price must be set
                    require(createOrderRequest.price != null && createOrderRequest.price >= BigDecimal.ZERO) {
                        "Price must be set for limit ASK IOC_BUDGET orders"
                    }
                }

                // For ASK orders, totalBudget is in base currency (e.g., ETH)
                if (createOrderRequest.totalBudget > pairSetting.maxOrder ||
                    createOrderRequest.totalBudget < pairSetting.minOrder) {
                    throw OpexError.InvalidQuantity.exception()
                }
            } else {
                // For BID orders, price should always be null for IOC_BUDGET
                require(createOrderRequest.price == null) { "Price must be null for BID IOC_BUDGET orders" }

                // For BID orders, totalBudget is in quote currency (e.g., USDT)
                if (createOrderRequest.totalBudget > pairSetting.maxOrder ||
                    createOrderRequest.totalBudget < pairSetting.minOrder) {
                    throw OpexError.InvalidQuantity.exception()
                }
            }
        } else {
            // Validation for non-IOC_BUDGET orders
            val orderValue = createOrderRequest.quantity * createOrderRequest.price!!
            if (orderValue > pairSetting.maxOrder || orderValue < pairSetting.minOrder) {
                throw OpexError.InvalidQuantity.exception()
            }
        }

// Determine which symbol to check with accountant
        val symbolSides = createOrderRequest.pair.split("_")
        val symbol = if (createOrderRequest.direction == OrderDirection.ASK) {
            // For ASK orders, we're selling the base currency
            symbolSides[0]
        } else {
            // For BID orders, we're buying with the quote currency
            symbolSides[1]
        }

        val pairConfig = pairConfigLoader.load(createOrderRequest.pair, createOrderRequest.direction)

        val canCreateOrder = runCatching {
            accountantApiProxy.canCreateOrder(
                createOrderRequest.uuid!!,
                symbol,
                calculateOrderTotalAmount(createOrderRequest)
            )
        }.onFailure { logger.error(it.message) }.getOrElse { false }

        if (!canCreateOrder)
            throw OpexError.SubmitOrderForbiddenByAccountant.exception()

        // Prepare the order submit request
        // Prepare the order submit request
        val longPrice = if (createOrderRequest.matchConstraint == MatchConstraint.IOC_BUDGET) {
            // For IOC_BUDGET orders, price is only set for limit ASK orders
            if (createOrderRequest.direction == OrderDirection.ASK &&
                createOrderRequest.orderType == OrderType.LIMIT_ORDER) {
                createOrderRequest.price?.divide(pairConfig.rightSideFraction)?.longValueExact()
            } else {
                0L // Set to 0 for market orders and BID orders
            }
        } else {
            createOrderRequest.price?.divide(pairConfig.rightSideFraction)?.longValueExact()
        }

        val longQuantity = if (createOrderRequest.matchConstraint == MatchConstraint.IOC_BUDGET) {
            // For IOC_BUDGET orders, we need to estimate a quantity
            // This is a "maximum possible quantity" that won't be exceeded
            if (createOrderRequest.direction == OrderDirection.ASK) {
                // For ASK orders, budget is in base currency, so quantity = budget
                createOrderRequest.totalBudget!!.divide(pairConfig.leftSideFraction).longValueExact()
            } else {
                // For BID orders, we need to estimate based on current market price
                // We'll use a very large quantity that would exceed the budget at any reasonable price
                // This ensures the matching engine will process the entire budget
                Long.MAX_VALUE / 1000 // Large but safe value
            }
        } else {
            createOrderRequest.quantity.divide(pairConfig.leftSideFraction).longValueExact()
        }

        val longBudget = createOrderRequest.totalBudget?.let { budget ->
            // Convert budget to the appropriate precision
            if (createOrderRequest.direction == OrderDirection.ASK) {
                // For ASK orders, budget is in base currency
                budget.divide(pairConfig.leftSideFraction).longValueExact()
            } else {
                // For BID orders, budget is in quote currency
                budget.divide(pairConfig.rightSideFraction).longValueExact()
            }
        }

        val orderSubmitRequest = OrderSubmitRequestEvent(
            createOrderRequest.uuid!!,
            Pair(symbolSides[0], symbolSides[1]),
            longPrice,
            longQuantity,
            createOrderRequest.direction,
            createOrderRequest.matchConstraint,
            createOrderRequest.orderType,
            createOrderRequest.userLevel,
            null,
            longBudget
        )
        return orderRequestEventSubmitter.submit(orderSubmitRequest)
    }

    private fun calculateOrderTotalAmount(createOrderRequest: CreateOrderRequest): BigDecimal? =
        if (createOrderRequest.matchConstraint == MatchConstraint.IOC_BUDGET) {
            createOrderRequest.totalBudget
        } else {
            if (createOrderRequest.direction == OrderDirection.ASK)
                createOrderRequest.quantity
            else
                createOrderRequest.quantity.multiply(createOrderRequest.price)
        }

    suspend fun cancelOrder(request: CancelOrderRequest): OrderSubmitResult {
        val symbols = request.symbol.split("_")
        val event = OrderCancelRequestEvent(request.ouid, request.uuid, Pair(symbols[0], symbols[1]), request.orderId)
        return orderRequestEventSubmitter.submit(event)
    }
}
