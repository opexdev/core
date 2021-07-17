package co.nilin.mixchange.port.api.postgres.impl

import co.nilin.mixchange.accountant.core.inout.OrderStatus
import co.nilin.mixchange.accountant.core.inout.RichOrder
import co.nilin.mixchange.api.core.spi.OrderPersister
import co.nilin.mixchange.port.api.postgres.dao.OrderRepository
import co.nilin.mixchange.port.api.postgres.model.OrderModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OrderPersisterImpl(val orderRepository: OrderRepository) : OrderPersister {
    override suspend fun save(order: RichOrder) {
        val existingOrder = orderRepository
                .findByOuid(order.ouid)
                .awaitFirstOrNull()
        if (existingOrder == null
                || existingOrder.executedQuantity?.compareTo(order.executedQuantity.toDouble()) == -1
                || order.status == OrderStatus.REJECTED.code
                || order.status == OrderStatus.CANCELED.code
                || order.status == OrderStatus.EXPIRED.code
                || order.status == OrderStatus.FILLED.code
                || existingOrder.type == null
        )
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
                            order.price.toDouble(),
                            order.quantity.toDouble(),
                            order.quoteQuantity.toDouble(),
                            order.executedQuantity.toDouble(),
                            order.accumulativeQuoteQty.toDouble(),
                            order.status,
                            existingOrder?.createDate ?: LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            ).awaitFirstOrNull()
    }
}