package co.nilin.opex.accountant.core.inout

import java.math.BigDecimal

data class RichOrderUpdate(
    val ouid: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val remainedQuantity: BigDecimal,
    val status: OrderStatus = OrderStatus.NEW
) : RichOrderEvent {

    fun executedQuantity(): BigDecimal = quantity.minus(remainedQuantity)

    fun accumulativeQuoteQuantity(): BigDecimal = price.multiply((quantity.minus(remainedQuantity)))

}