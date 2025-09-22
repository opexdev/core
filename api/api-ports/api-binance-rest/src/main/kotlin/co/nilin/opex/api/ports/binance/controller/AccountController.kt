package co.nilin.opex.api.ports.binance.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.binance.data.*
import co.nilin.opex.api.ports.binance.util.*
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.common.security.tokenValue
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.time.ZoneId
import java.util.*

@RestController
class AccountController(
    val queryHandler: MarketUserDataProxy,
    val matchingGatewayProxy: MatchingGatewayProxy,
    val walletProxy: WalletProxy,
    val symbolMapper: SymbolMapper
) {

    /*
    Send in a new order.
    Weight: 1
    Data Source: Matching Engine
    */
    @PostMapping(
        "/v3/order",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"symbol\": \"btc_usdt\", \"orderId\": -1, \"orderListId\": -1, \"transactTime\": \"2021-08-03T11:09:23.190+00:00\" }",
                mediaType = "application/json"
            )
        )
    )
    fun createNewOrder(
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
        @ApiParam(
            value = "A unique id among open orders. Automatically generated if not sent.\n" +
                    "Orders with the same newClientOrderID can be accepted only when the previous one is filled, otherwise the order will be rejected."
        )
        @RequestParam(required = false)
        newClientOrderId: String?,    /* A unique id among open orders. Automatically generated if not sent.
    Orders with the same newClientOrderID can be accepted only when the previous one is filled, otherwise the order will be rejected.
    */
        @ApiParam(value = "Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.")
        @RequestParam(required = false)
        stopPrice: BigDecimal?, //Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.
        @RequestParam(required = false)
        @ApiParam(value = "Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.")
        icebergQty: BigDecimal?, //Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.
        @RequestParam(required = false)
        @ApiParam(value = "Set the response JSON. ACK, RESULT, or FULL; MARKET and LIMIT order types default to FULL, all other orders default to ACK.")
        newOrderRespType: OrderResponseType?,  //Set the response JSON. ACK, RESULT, or FULL; MARKET and LIMIT order types default to FULL, all other orders default to ACK.
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): NewOrderResponse {
        val internalSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        validateNewOrderParams(type, price, quantity, timeInForce, stopPrice, quoteOrderQty)

        matchingGatewayProxy.createNewOrder(
            securityContext.jwtAuthentication().name,
            internalSymbol,
            price ?: BigDecimal.ZERO, // Maybe make this nullable as well?
            quantity ?: BigDecimal.ZERO,
            side.asOrderDirection(),
            timeInForce?.asMatchConstraint(),
            type.asMatchingOrderType(),
            "*",
            securityContext.jwtAuthentication().tokenValue()
        )
        return NewOrderResponse(
            symbol,
            -1,
            -1,
            null,
            Date(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    @DeleteMapping(
        "/v3/order",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun cancelOrder(
        principal: Principal,
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        orderId: Long?, //Either orderId or origClientOrderId must be sent.
        @RequestParam(required = false)
        origClientOrderId: String?,
        @RequestParam(required = false)
        newClientOrderId: String?,
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): CancelOrderResponse {
        val localSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        if (orderId == null && origClientOrderId == null)
            throw OpexError.BadRequest.exception("'orderId' or 'origClientOrderId' must be sent")

        val order = queryHandler.queryOrder(principal, localSymbol, orderId, origClientOrderId)
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
            localSymbol,
            securityContext.jwtAuthentication().tokenValue()
        )
        return response
    }

    /*
  Check an order's status.

  Weight: 2
  Data Source: Database
  */
    @GetMapping(
        "/v3/order",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"symbol\": \"btc_usdt\", \"orderId\": 12, \"orderListId\": -1, \"clientOrderId\": \"\", \"price\": 1, \"origQty\": 10, \"executedQty\": 0, \"cummulativeQuoteQty\": 0, \"status\": \"NEW\", \"timeInForce\": \"GTC\", \"type\": \"LIMIT\", \"side\": \"SELL\", \"time\": \"2021-08-04T12:10:13.488+00:00\", \"updateTime\": \"2021-08-04T12:10:13.488+00:00\", \"isWorking\": true, \"origQuoteOrderQty\": 10 }",
                mediaType = "application/json"
            )
        )
    )
    fun queryOrder(
        principal: Principal,
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        orderId: Long?,
        @RequestParam(required = false)
        origClientOrderId: String?,
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long
    ): QueryOrderResponse {
        val internalSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        return queryHandler.queryOrder(principal, internalSymbol, orderId, origClientOrderId)
            ?.asQueryOrderResponse()
            ?.apply { this.symbol = symbol }
            ?: throw OpexError.OrderNotFound.exception()
    }

    /*
      Get all open orders on a symbol. Careful when accessing this with no symbol.

      Weight: 3 for a single symbol; 40 when the symbol parameter is omitted

      Data Source: Database
    */
    @GetMapping(
        "/v3/openOrders",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "[ { \"symbol\": \"btc_usdt\", \"orderId\": 12, \"orderListId\": -1, \"clientOrderId\": \"\", \"price\": 1, \"origQty\": 10, \"executedQty\": 0, \"cummulativeQuoteQty\": 0, \"status\": \"NEW\", \"timeInForce\": \"GTC\", \"type\": \"LIMIT\", \"side\": \"SELL\", \"time\": \"2021-08-04T12:10:13.488+00:00\", \"updateTime\": \"2021-08-04T12:10:13.488+00:00\", \"isWorking\": true, \"origQuoteOrderQty\": 10 } ]",
                mediaType = "application/json"
            )
        )
    )
    fun fetchOpenOrders(
        principal: Principal,
        @RequestParam(required = false)
        symbol: String?,
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long,
        @RequestParam(required = false)
        limit: Int?
    ): List<QueryOrderResponse> {
        val internalSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        return queryHandler.openOrders(principal, internalSymbol, limit).map {
            it.asQueryOrderResponse().apply { symbol?.let { s -> this.symbol = s } }
        }
    }

    /*
   Get all account orders; active, canceled, or filled.
   Weight: 10 with symbol
   Data Source: Database
   */
    @GetMapping(
        "/v3/allOrders",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    fun fetchAllOrders(
        principal: Principal,
        @RequestParam(required = false)
        symbol: String?,
        @RequestParam(required = false)
        startTime: Date?,
        @RequestParam(required = false)
        endTime: Date?,
        @ApiParam(value = "Default 500; max 1000.")
        @RequestParam(required = false)
        limit: Int?, //Default 500; max 1000.
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long
    ): List<QueryOrderResponse> {
        val internalSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        return queryHandler.allOrders(principal, internalSymbol, startTime, endTime, limit).map {
            it.asQueryOrderResponse().apply { symbol?.let { s -> this.symbol = s } }
        }
    }

    /*
    Get trades for a specific account and symbol.
    If fromId is set, it will get trades >= that fromId. Otherwise, most recent trades are returned.
    Weight: 10 with symbol
    Data Source: Database
    */
    @GetMapping(
        "/v3/myTrades",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ }",
                mediaType = "application/json"
            )
        )
    )
    fun fetchAllTrades(
        principal: Principal,
        @RequestParam
        symbol: String?,
        @RequestParam(required = false)
        startTime: Date?,
        @RequestParam(required = false)
        endTime: Date?,
        @ApiParam(value = "TradeId to fetch from. Default gets most recent trades.")
        @RequestParam(required = false)
        fromId: Long?,//TradeId to fetch from. Default gets most recent trades.
        @ApiParam(value = "Default 500; max 1000.")
        @RequestParam(required = false)
        limit: Int?, //Default 500; max 1000.
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long
    ): List<TradeResponse> {
        val internalSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()

        return queryHandler.allTrades(principal, internalSymbol, fromId, startTime, endTime, limit)
            .map {
                TradeResponse(
                    symbol ?: "",
                    it.id,
                    it.orderId,
                    -1,
                    it.price,
                    it.quantity,
                    it.quoteQuantity,
                    it.commission,
                    it.commissionAsset,
                    it.time,
                    it.isBuyer,
                    it.isMaker,
                    it.isBestMatch
                )
            }
    }

    @GetMapping(
        "/v3/account",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiResponse(
        message = "OK",
        code = 200,
        examples = Example(
            ExampleProperty(
                value = "{ \"makerCommission\": 0, \"takerCommission\": 0, \"buyerCommission\": 0, \"sellerCommission\": 0, \"canTrade\": true, \"canWithdraw\": true, \"canDeposit\": true, \"updateTime\": 1628420513843, \"accountType\": \"SPOT\", \"balances\": [ { \"asset\": \"usdt\", \"free\": 1000, \"locked\": 0 } ], \"permissions\": [ \"SPOT\" ] }",
                mediaType = "application/json"
            )
        )
    )
    fun accountInfo(
        @CurrentSecurityContext securityContext: SecurityContext,
        @ApiParam(value = "The value cannot be greater than 60000")
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long
    ): AccountInfoResponse {
        val auth = securityContext.jwtAuthentication()
        val wallets = walletProxy.getWallets(auth.name, auth.tokenValue())
        val limits = walletProxy.getOwnerLimits(auth.name, auth.tokenValue())
        val accountType = "SPOT"

        //TODO replace commissions and accountType with actual data
        return AccountInfoResponse(
            0,
            0,
            0,
            0,
            limits.canTrade,
            limits.canWithdraw,
            limits.canDeposit,
            Date().time,
            accountType,
            wallets.map { BalanceResponse(it.asset, it.balance, it.locked, it.withdraw) },
            listOf(accountType)
        )
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