package co.nilin.opex.api.ports.opex.data

import co.nilin.opex.api.core.inout.OrderSide
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.core.inout.OrderType
import co.nilin.opex.api.core.inout.TimeInForce
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NewOrderResponse(
    val symbol: String,
    val orderId: Long,
    val orderListId: Long, //Unless OCO, value will be -1
    val clientOrderId: String?,
    val transactTime: Date,
    val price: BigDecimal?,
    val origQty: BigDecimal?,
    val executedQty: BigDecimal?,
    val cummulativeQuoteQty: BigDecimal?,
    val status: OrderStatus?,
    val timeInForce: TimeInForce?,
    val type: OrderType?,
    val side: OrderSide?,
    val fills: List<FillsData>?
)