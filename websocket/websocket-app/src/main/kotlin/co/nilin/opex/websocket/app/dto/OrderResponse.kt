package co.nilin.opex.websocket.app.dto

import co.nilin.opex.websocket.core.inout.OrderSide
import co.nilin.opex.websocket.core.inout.OrderStatus
import co.nilin.opex.websocket.core.inout.OrderType
import co.nilin.opex.websocket.core.inout.TimeInForce
import java.math.BigDecimal
import java.util.*

data class OrderResponse(
    val ouid:String,
    val symbol: String,
    val orderId: Long,
    val orderListId: Long,
    val clientOrderId: String?,
    val price: BigDecimal,
    val origQty: BigDecimal,
    val executedQty: BigDecimal,
    val accumulativeQuoteQty: BigDecimal,
    val status: OrderStatus?,
    val timeInForce: TimeInForce?,
    val type: OrderType?,
    val side: OrderSide?,
    val time: Date,
    val updateTime: Date,
    val isWorking: Boolean,
    val origQuoteOrderQty: BigDecimal
)
