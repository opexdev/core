package co.nilin.opex.port.api.postgres.impl

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.comesAfter
import co.nilin.opex.api.core.spi.OrderPersister
import co.nilin.opex.port.api.postgres.dao.OrderRepository
import co.nilin.opex.port.api.postgres.model.OrderModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OrderPersisterImpl(val orderRepository: OrderRepository) : OrderPersister {
    override suspend fun save(order: RichOrder) {
        var alreadySaved = false
        val existingOrder = orderRepository
            .findByOuid(order.ouid)
            .awaitFirstOrNull()
        if (existingOrder == null
            || existingOrder.executedQuantity?.compareTo(order.executedQuantity.toDouble()) == -1
            || order.status.comesAfter(existingOrder.status)
        ) {
            alreadySaved = true
            orderRepository.save(
                OrderModel(
                    existingOrder?.id,
                    order.ouid,
                    order.uuid,
                    null,
                    order.pair,
                    order.orderId,
                    order.makerFee.toDouble(),
                    order.takerFee.toDouble(),
                    order.leftSideFraction.toDouble(),
                    order.rightSideFraction.toDouble(),
                    order.userLevel,
                    order.direction,
                    order.constraint,
                    order.type,
                    existingOrder?.price ?: order.price.toDouble(),
                    existingOrder?.quantity ?: order.quantity.toDouble(),
                    existingOrder?.quoteQuantity ?: order.quoteQuantity.toDouble(),
                    existingOrder?.executedQuantity ?: order.executedQuantity.toDouble(),
                    existingOrder?.accumulativeQuoteQty ?: order.accumulativeQuoteQty.toDouble(),
                    order.status,
                    existingOrder?.createDate ?: LocalDateTime.now(),
                    LocalDateTime.now()
                )
            ).awaitFirstOrNull()
        }

        existingOrder?.apply {
            if (
                !alreadySaved &&
                (makerFee == null || takerFee == null || leftSideFraction == null
                        || rightSideFraction == null || constraint == null || type == null)
            ) {
                orderRepository.save(
                    OrderModel(
                        existingOrder.id,
                        existingOrder.ouid,
                        existingOrder.uuid,
                        null,
                        existingOrder.symbol,
                        existingOrder.orderId,
                        order.makerFee.toDouble(),
                        order.takerFee.toDouble(),
                        order.leftSideFraction.toDouble(),
                        order.rightSideFraction.toDouble(),
                        order.userLevel,
                        order.direction,
                        order.constraint,
                        order.type,
                        existingOrder.price,
                        existingOrder.quantity,
                        existingOrder.quoteQuantity,
                        existingOrder.executedQuantity,
                        existingOrder.accumulativeQuoteQty,
                        existingOrder.status,
                        existingOrder.createDate ?: LocalDateTime.now(),
                        existingOrder.updateDate
                    )
                ).awaitFirstOrNull()
            }
        }
    }
}