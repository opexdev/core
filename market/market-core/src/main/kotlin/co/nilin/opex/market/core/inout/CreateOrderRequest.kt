package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class CreateOrderRequest(
    val symbol: String,
    val side: OrderSide,
    val type: OrderType,
    val timeInForce: TimeInForce?,
    val quantity: BigDecimal?,
    val quoteOrderQty: BigDecimal?,
    val price: BigDecimal?,
    val newClientOrderId: String?,    /* A unique id among open orders. Automatically generated if not sent.
    Orders with the same newClientOrderID can be accepted only when the previous one is filled, otherwise the order will be rejected.
    */
    val stopPrice: BigDecimal?, //Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.
    val icebergQty: BigDecimal?, //Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.
    val newOrderRespType: OrderResponseType?,  //Set the response JSON. ACK, RESULT, or FULL; MARKET and LIMIT order types default to FULL, all other orders default to ACK.
)