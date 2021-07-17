package co.nilin.mixchange.api.core.inout

import java.math.BigDecimal
import java.util.*

data class CreateOrderResponse(
        val symbol: String,
        val orderId: Long,
        val orderListId: Long, //Unless OCO, value will be -1
        val clientOrderId: String,
        val transactTime: Date,
        val price: BigDecimal?,
        val origQty: BigDecimal?,
        val executedQty: BigDecimal?,
        val cummulativeQuoteQty: BigDecimal,
        val status: OrderStatus?,
        val timeInForce: TimeInForce?,
        val type: OrderType?,
        val side: OrderSide?,
        val fills: List<OrderTradeData>?
)