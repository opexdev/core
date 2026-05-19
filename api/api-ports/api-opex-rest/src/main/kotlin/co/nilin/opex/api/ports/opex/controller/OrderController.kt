package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.ports.opex.data.CancelOrderResponse
import co.nilin.opex.api.ports.opex.data.NewOrderResponse
import co.nilin.opex.api.ports.opex.data.QueryOrderResponse
import co.nilin.opex.api.ports.opex.util.*
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.common.security.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.time.ZoneId
import java.util.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/opex/v1/order")
@Tag(name = "Order", description = "Authenticated order creation, cancellation, query, and open order operations.")
class OrderController(
    val queryHandler: MarketUserDataProxy,
    val matchingGatewayProxy: MatchingGatewayProxy
) {
    @PostMapping
    @Operation(
        summary = "Create new order",
        description = """POST /opex/v1/order.
Security: Bearer user-token required. Required authority: PERM_order:write.

Validation: For LIMIT orders, price and quantity are expected. For MARKET orders, use the quantity/quoteOrderQty combination supported by the backend validation. Stop order types require stopPrice.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = NewOrderResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: PERM_order:write. No response body.", content = [Content()])
        ]
    )
    suspend fun createNewOrder(
        @RequestParam
        symbol: String,
        @RequestParam
        side: OrderSide,
        @RequestParam
        type: OrderType,
        @RequestParam(required = false)
        timeInForce: TimeInForce?,
        @RequestParam(required = false)
        quantity: BigDecimal?,
        @RequestParam(required = false)
        quoteOrderQty: BigDecimal?,
        @RequestParam(required = false)
        price: BigDecimal?,
        @Parameter(description = "Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.")
        @RequestParam(required = false)
        stopPrice: BigDecimal?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): NewOrderResponse {
        validateNewOrderParams(type, price, quantity, timeInForce, stopPrice, quoteOrderQty)

        matchingGatewayProxy.createNewOrder(
            securityContext.jwtAuthentication().name,
            symbol,
            price ?: BigDecimal.ZERO,
            quantity ?: BigDecimal.ZERO,
            side.asOrderDirection(),
            timeInForce?.asMatchConstraint(),
            type.asMatchingOrderType(),
            "*",
            securityContext.jwtAuthentication().tokenValue()
        )
        return NewOrderResponse(symbol)
    }

    @PutMapping
    @Operation(
        summary = "Cancel order",
        description = """PUT /opex/v1/order.
Security: Bearer user-token required. Required authority: PERM_order:write.

Validation: Either `orderId` or `origClientOrderId` must be provided.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = CancelOrderResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Forbidden. Required authority is missing: PERM_order:write. No response body.", content = [Content()])
        ]
    )
    suspend fun cancelOrder(
        principal: Principal,
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        orderId: Long?,
        @RequestParam(required = false)
        origClientOrderId: String?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): CancelOrderResponse {
        if (orderId == null && origClientOrderId == null)
            throw OpexError.BadRequest.exception("'orderId' or 'origClientOrderId' must be sent")

        val order = queryHandler.queryOrder(principal, symbol, orderId, origClientOrderId)
            ?: throw OpexError.OrderNotFound.exception()

        val response = CancelOrderResponse(
            symbol,
            origClientOrderId,
            orderId,
            -1,
            null,
            order.price,
            order.quantity,
            order.executedQuantity,
            order.accumulativeQuoteQty,
            OrderStatus.CANCELED,
            order.constraint.asTimeInForce(),
            order.type.asOrderType(),
            order.direction.asOrderSide()
        )

        if (order.status == OrderStatus.CANCELED)
            return response

        if (order.status.equalsAny(OrderStatus.REJECTED, OrderStatus.EXPIRED, OrderStatus.FILLED))
            throw OpexError.CancelOrderNotAllowed.exception()

        matchingGatewayProxy.cancelOrder(
            order.ouid,
            principal.name,
            order.orderId ?: 0,
            symbol,
            securityContext.jwtAuthentication().tokenValue()
        )
        return response
    }

    @GetMapping
    @Operation(
        summary = "Query order",
        description = """GET /opex/v1/order.
Security: Bearer user-token required. Requires authenticated user JWT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = QueryOrderResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun queryOrder(
        principal: Principal,
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        orderId: Long?,
        @RequestParam(required = false)
        origClientOrderId: String?
    ): QueryOrderResponse {
        return queryHandler.queryOrder(principal, symbol, orderId, origClientOrderId)
            ?.asQueryOrderResponse()
            ?.apply { this.symbol = symbol }
            ?: throw OpexError.OrderNotFound.exception()
    }

    @GetMapping("/open")
    @Operation(
        summary = "Fetch open orders",
        description = """GET /opex/v1/order/open.
Security: Bearer user-token required. Requires authenticated user JWT.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = QueryOrderResponse::class)))]),
            ApiResponse(responseCode = "401", description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.", content = [Content()])
        ]
    )
    suspend fun fetchOpenOrders(
        principal: Principal,
        @RequestParam(required = false)
        symbol: String?,
        @RequestParam(required = false)
        limit: Int?
    ): List<QueryOrderResponse> {
        return queryHandler.openOrders(principal, symbol, limit).map {
            it.asQueryOrderResponse().apply { symbol?.let { s -> this.symbol = s } }
        }
    }

    private fun validateNewOrderParams(
        type: OrderType,
        price: BigDecimal?,
        quantity: BigDecimal?,
        timeInForce: TimeInForce?,
        stopPrice: BigDecimal?,
        quoteOrderQty: BigDecimal?
    ) {
        when (type) {
            OrderType.LIMIT -> {
                checkDecimal(price, "price")
                checkDecimal(quantity, "quantity")
                checkNull(timeInForce, "timeInForce")
            }

            OrderType.MARKET -> {
                if (quantity == null)
                    checkDecimal(quoteOrderQty, "quoteOrderQty")
                else
                    checkDecimal(quantity, "quantity")
            }

            OrderType.STOP_LOSS -> {
                checkDecimal(quantity, "quantity")
                checkDecimal(stopPrice, "stopPrice")
            }

            OrderType.STOP_LOSS_LIMIT -> {
                checkDecimal(price, "price")
                checkDecimal(quantity, "quantity")
                checkDecimal(stopPrice, "stopPrice")
                checkNull(timeInForce, "timeInForce")
            }

            OrderType.TAKE_PROFIT -> {
                checkDecimal(quantity, "quantity")
                checkDecimal(stopPrice, "stopPrice")
            }

            OrderType.TAKE_PROFIT_LIMIT -> {
                checkDecimal(price, "price")
                checkDecimal(quantity, "quantity")
                checkDecimal(stopPrice, "stopPrice")
                checkNull(timeInForce, "timeInForce")
            }

            OrderType.LIMIT_MAKER -> {
                checkDecimal(price, "price")
                checkDecimal(quantity, "quantity")
            }
        }
    }

    private fun checkDecimal(decimal: BigDecimal?, paramName: String) {
        if (decimal == null || decimal <= BigDecimal.ZERO)
            throw OpexError.InvalidRequestParam.exception("Parameter '$paramName' is either missing or invalid")
    }

    private fun checkNull(obj: Any?, paramName: String) {
        if (obj == null)
            throw OpexError.InvalidRequestParam.exception("Parameter '$paramName' is either missing or invalid")
    }

    private fun Order.asQueryOrderResponse() = QueryOrderResponse(
        symbol,
        ouid,
        orderId ?: 0,
        -1,
        "",
        price,
        quantity,
        executedQuantity,
        accumulativeQuoteQty,
        status,
        constraint.asTimeInForce(),
        type.asOrderType(),
        direction.asOrderSide(),
        null,
        null,
        Date.from(createDate.atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(updateDate.atZone(ZoneId.systemDefault()).toInstant()),
        status.isWorking(),
        quoteQuantity
    )

}
