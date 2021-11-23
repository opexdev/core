package co.nilin.opex.port.websocket.dto

import co.nilin.opex.websocket.core.inout.OrderSide
import co.nilin.opex.websocket.core.inout.OrderStatus
import co.nilin.opex.websocket.core.inout.OrderType
import co.nilin.opex.websocket.core.inout.TimeInForce
import java.math.BigDecimal
import java.util.*

data class OrderResponse(
    val symbol: String,
    val orderId: Long,
    val orderListId: Long, //Unless part of an OCO, the value will always be -1.
    val clientOrderId: String?,
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
