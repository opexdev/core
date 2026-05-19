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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.time.ZoneId
import java.util.*

@RestController
@RequestMapping("/opex/v1/order")
@Tag(
    name = "Order",
    description = "Create, cancel, query, and list authenticated user's orders."
)
class OrderController(
    val queryHandler: MarketUserDataProxy,
    val matchingGatewayProxy: MatchingGatewayProxy,
) {

    @PostMapping
    @Operation(
        summary = "Create order",
        description = """
Security:
- Bearer user-token is required.
- Required permission: PERM_order:write.

Validation:
- symbol, side, and type are required.
- side values: BUY, SELL.
- type values: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- timeInForce values: GTC, IOC, FOK.
- LIMIT(*) requires price, quantity, and timeInForce.
- MARKET requires quantity or quoteOrderQty.
- STOP_LOSS requires quantity and stopPrice.
- STOP_LOSS_LIMIT requires price, quantity, stopPrice, and timeInForce.
- TAKE_PROFIT requires quantity and stopPrice.
- TAKE_PROFIT_LIMIT requires price, quantity, stopPrice, and timeInForce.
- LIMIT_MAKER requires price and quantity.

Behavior:
- Optional parameters that are not applicable to the selected order type should be omitted.
- Do not send the literal string "null" for optional numeric parameters.

Response body:
- NewOrderResponse.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = NewOrderResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun createNewOrder(
        @Parameter(
            name = "symbol",
            description = "Trading pair symbol.",
            required = true,
            `in` = ParameterIn.QUERY,
            example = "BTC_USDT"
        )
        @RequestParam symbol: String,

        @Parameter(
            name = "side",
            description = "Order side. Values: BUY, SELL.",
            required = true,
            `in` = ParameterIn.QUERY,
            schema = Schema(implementation = OrderSide::class)
        )
        @RequestParam side: OrderSide,

        @Parameter(
            name = "type",
            description = "Order type. Values: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.",
            required = true,
            `in` = ParameterIn.QUERY,
            schema = Schema(implementation = OrderType::class)
        )
        @RequestParam type: OrderType,

        @Parameter(
            name = "timeInForce",
            description = "Optional time-in-force. Values: GTC, IOC, FOK. Required for LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT orders.",
            required = false,
            `in` = ParameterIn.QUERY,
            schema = Schema(implementation = TimeInForce::class)
        )
        @RequestParam(required = false) timeInForce: TimeInForce?,

        @Parameter(
            name = "quantity",
            description = "Optional base quantity. Required for LIMIT, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, and LIMIT_MAKER orders. For MARKET orders, quantity or quoteOrderQty must be provided.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "0.001"
        )
        @RequestParam(required = false) quantity: BigDecimal?,

        @Parameter(
            name = "quoteOrderQty",
            description = "Optional quote quantity for MARKET orders when quantity is not provided.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "10.0"
        )
        @RequestParam(required = false) quoteOrderQty: BigDecimal?,

        @Parameter(
            name = "price",
            description = "Optional order price. Required for LIMIT, STOP_LOSS_LIMIT, TAKE_PROFIT_LIMIT, and LIMIT_MAKER orders.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "10.0"
        )
        @RequestParam(required = false) price: BigDecimal?,

        @Parameter(
            name = "stopPrice",
            description = "Optional stop price. Required for STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "9.5"
        )
        @RequestParam(required = false) stopPrice: BigDecimal?,

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
        description = """
Security:
- Bearer user-token is required.
- Required permission: PERM_order:write.

Validation:
- symbol is required.
- At least one lookup identifier is required: orderId or origClientOrderId.

Behavior:
- Already canceled orders return a canceled response.
- Rejected, expired, or filled orders cannot be canceled.

Response body:
- CancelOrderResponse.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order canceled successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CancelOrderResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun cancelOrder(
        @Parameter(hidden = true)
        principal: Principal,

        @Parameter(
            name = "symbol",
            description = "Trading pair symbol.",
            required = true,
            `in` = ParameterIn.QUERY,
            example = "BTC_USDT"
        )
        @RequestParam symbol: String,

        @Parameter(
            name = "orderId",
            description = "Optional numeric order ID. Required when origClientOrderId is not provided.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "1"
        )
        @RequestParam(required = false) orderId: Long?,

        @Parameter(
            name = "origClientOrderId",
            description = "Optional original client order ID. Required when orderId is not provided.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "client-order-id-sample"
        )
        @RequestParam(required = false) origClientOrderId: String?,

        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): CancelOrderResponse {
        if (orderId == null && origClientOrderId == null) throw OpexError.BadRequest.exception("'orderId' or 'origClientOrderId' must be sent")
        val order = queryHandler.queryOrder(principal, symbol, orderId, origClientOrderId) ?: throw OpexError.OrderNotFound.exception()
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
        if (order.status == OrderStatus.CANCELED) return response
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
        summary = "Get order",
        description = """
Security:
- Bearer user-token is required.

Validation:
- symbol is required.
- At least one lookup identifier should be provided: orderId or origClientOrderId.

Response body:
- QueryOrderResponse.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = QueryOrderResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun queryOrder(
        @Parameter(hidden = true)
        principal: Principal,

        @Parameter(
            name = "symbol",
            description = "Trading pair symbol.",
            required = true,
            `in` = ParameterIn.QUERY,
            example = "BTC_USDT"
        )
        @RequestParam symbol: String,

        @Parameter(
            name = "orderId",
            description = "Optional numeric order ID.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "1"
        )
        @RequestParam(required = false) orderId: Long?,

        @Parameter(
            name = "origClientOrderId",
            description = "Optional original client order ID.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "client-order-id-sample"
        )
        @RequestParam(required = false) origClientOrderId: String?,
    ): QueryOrderResponse {
        return queryHandler.queryOrder(principal, symbol, orderId, origClientOrderId)
            ?.asQueryOrderResponse()
            ?.apply { this.symbol = symbol }
            ?: throw OpexError.OrderNotFound.exception()
    }

    @GetMapping("/open")
    @Operation(
        summary = "List open orders",
        description = """
Security:
- Bearer user-token is required.

Behavior:
- symbol is optional. When omitted, open orders are returned without symbol filtering.
- limit is optional.

Response body:
- Array of QueryOrderResponse.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Open orders returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = QueryOrderResponse::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun fetchOpenOrders(
        @Parameter(hidden = true)
        principal: Principal,

        @Parameter(
            name = "symbol",
            description = "Optional trading pair symbol filter.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "BTC_USDT"
        )
        @RequestParam(required = false) symbol: String?,

        @Parameter(
            name = "limit",
            description = "Optional maximum number of open orders to return.",
            required = false,
            `in` = ParameterIn.QUERY,
            example = "10"
        )
        @RequestParam(required = false) limit: Int?
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
        quoteOrderQty: BigDecimal?,
    ) {
        when (type) {
            OrderType.LIMIT -> {
                checkDecimal(price, "price")
                checkDecimal(quantity, "quantity")
                checkNull(timeInForce, "timeInForce")
            }
            OrderType.MARKET -> {
                if (quantity == null) checkDecimal(quoteOrderQty, "quoteOrderQty")
                else checkDecimal(quantity, "quantity")
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
