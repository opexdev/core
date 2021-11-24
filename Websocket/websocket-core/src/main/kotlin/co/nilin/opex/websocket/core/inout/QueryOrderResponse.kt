package co.nilin.opex.websocket.core.inout

import java.math.BigDecimal
import java.util.*

data class QueryOrderResponse(
    val symbol: String,
    val ouid: String,
    val orderId: Long,
    val orderListId: Long, //Unless part of an OCO, the value will always be -1.
    val clientOrderId: String,
    val price: BigDecimal,
    val origQty: BigDecimal,
    val executedQty: BigDecimal,
    val cummulativeQuoteQty: BigDecimal,
    val status: OrderStatus,
    val timeInForce: TimeInForce,
    val type: OrderType,
    val side: OrderSide,
    val stopPrice: BigDecimal?,
    val icebergQty: BigDecimal?,
    val time: Date,
    val updateTime: Date,
    val isWorking: Boolean,
    val origQuoteOrderQty: BigDecimal
)