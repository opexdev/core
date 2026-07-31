package co.nilin.opex.matching.engine.core.util

import co.nilin.opex.matching.engine.core.inout.*

 fun OrderRequestEvent.toCommand(): OrderCommand =
    when (this) {
        is OrderSubmitRequestEvent ->
            OrderCreateCommand(
                ouid = ouid,
                uuid = uuid,
                pair = pair,
                price = price,
                quantity = quantity,
                direction = direction,
                matchConstraint = matchConstraint,
                orderType = orderType
            )

        is OrderCancelRequestEvent ->
            OrderCancelCommand(
                ouid = ouid,
                uuid = uuid,
                orderId = orderId,
                pair = pair
            )

        else ->
            throw IllegalArgumentException(
                "Unsupported order request type: ${this::class.java.name}"
            )
    }