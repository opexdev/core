package co.nilin.mixchange.port.accountant.postgres.impl

import co.nilin.mixchange.accountant.core.model.Order
import co.nilin.mixchange.accountant.core.spi.OrderPersister
import co.nilin.mixchange.port.accountant.postgres.dao.OrderRepository
import co.nilin.mixchange.port.accountant.postgres.model.OrderModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

@Component
class OrderPersisterImpl(val orderRepository: OrderRepository) : OrderPersister {
    override suspend fun load(ouid: String): Order? {
        val model = orderRepository.findByOuid(ouid)
            .awaitFirstOrNull() ?: return null
        return Order(model.pair, model.ouid, model.matchingEngineId, model.makerFee, model.takerFee
        , model.leftSideFraction, model.rightSideFraction, model.uuid, model.userLevel
        , model.direction, model.price, model.quantity, model.filledQuantity
        , model.firstTransferAmount, model.remainedTransferAmount, model.status, model.id)
    }

    override suspend fun save(order: Order): Order {
        orderRepository.save(
            OrderModel(
                order.id,
                order.ouid,
                order.uuid,
                order.pair,
                order.matchingEngineId,
                order.makerFee,
                order.takerFee,
                order.leftSideFraction,
                order.rightSideFraction,
                order.userLevel,
                order.direction,
                order.price,
                order.quantity,
                order.filledQuantity,
                order.firstTransferAmount,
                order.remainedTransferAmount,
                order.status,
                "",
                "",
                LocalDateTime.now()
            )
        ).awaitFirstOrNull()
        return order
    }
}