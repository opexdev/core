package co.nilin.opex.market.ports.postgres.util

import co.nilin.opex.market.core.inout.Order
import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.ports.postgres.model.OrderModel
import co.nilin.opex.market.ports.postgres.model.OrderStatusModel

fun OrderModel.asOrderDTO(status: OrderStatusModel?) = Order(
    id!!,
    ouid,
    uuid,
    clientOrderId,
    symbol,
    orderId,
    makerFee,
    takerFee,
    leftSideFraction,
    rightSideFraction,
    userLevel,
    direction,
    constraint,
    type,
    price,
    quantity,
    quoteQuantity,
    status?.executedQuantity,
    status?.accumulativeQuoteQty,
    OrderStatus.fromCode(status?.status),
    createDate,
    updateDate
)