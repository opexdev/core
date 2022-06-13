package co.nilin.opex.market.ports.postgres.util

import co.nilin.opex.market.core.inout.Order
import co.nilin.opex.market.ports.postgres.model.OrderModel

fun OrderModel.asOrderDTO() = Order(
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
    createDate,
    updateDate
)