package co.nilin.opex.api.ports.opex.util

import co.nilin.opex.api.core.inout.OrderData
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.ports.opex.data.OrderDataResponse

fun OrderData.toResponse(): OrderDataResponse {
    return OrderDataResponse(
        symbol = this.symbol,
        orderId = this.orderId,
        orderType = this.orderType,
        side = this.side,
        price = this.price,
        quantity = this.quantity,
        quoteQuantity = this.quoteQuantity,
        executedQuantity = this.executedQuantity,
        takerFee = this.takerFee,
        makerFee = this.makerFee,
        status = OrderStatus.fromCode(this.status) ?: OrderStatus.REJECTED,
        createDate = this.createDate,
        updateDate = this.updateDate,
    )
}