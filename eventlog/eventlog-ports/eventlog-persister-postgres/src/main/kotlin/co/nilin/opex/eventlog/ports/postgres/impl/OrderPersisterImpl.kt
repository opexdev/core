package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.spi.OrderPersister
import co.nilin.opex.eventlog.ports.postgres.dao.OrderEventRepository
import co.nilin.opex.eventlog.ports.postgres.dao.OrderRepository
import co.nilin.opex.eventlog.ports.postgres.model.OrderEventsModel
import co.nilin.opex.eventlog.ports.postgres.model.OrderModel
import co.nilin.opex.matching.engine.core.eventh.events.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OrderPersisterImpl(
    val orderRepository: OrderRepository, val orderEventRepository: OrderEventRepository
) : OrderPersister {
    override suspend fun submitOrder(orderEvent: SubmitOrderEvent) {
        orderRepository.save(
            OrderModel(
                null,
                orderEvent.ouid,
                orderEvent.pair.toString(),
                orderEvent.direction.toString(),
                orderEvent.matchConstraint.toString(),
                orderEvent.orderType.toString(),
                orderEvent.uuid,
                "agent",
                "127.0.0.1",
                orderEvent.eventDate,
                LocalDateTime.now()
            )
        )
            .block()
        orderEventRepository.save(
            OrderEventsModel(
                null,
                orderEvent.ouid,
                orderEvent.uuid,
                orderEvent.orderId,
                orderEvent.price,
                orderEvent.quantity,
                orderEvent.quantity - orderEvent.remainedQuantity,
                orderEvent.javaClass.simpleName,
                "agent",
                "127.0.0.1",
                orderEvent.eventDate,
                LocalDateTime.now()
            )
        )
            .awaitFirst()

    }

    override suspend fun rejectOrder(orderEvent: RejectOrderEvent) {
        orderEventRepository.save(
            OrderEventsModel(
                null,
                orderEvent.ouid,
                orderEvent.uuid,
                orderEvent.orderId,
                orderEvent.price,
                orderEvent.quantity,
                0,
                orderEvent.javaClass.simpleName,
                "agent",
                "127.0.0.1",
                orderEvent.eventDate,
                LocalDateTime.now()
            )
        ).awaitFirst()
    }

    override suspend fun saveOrder(orderEvent: CreateOrderEvent) {
        orderEventRepository.save(
            OrderEventsModel(
                null,
                orderEvent.ouid,
                orderEvent.uuid,
                orderEvent.orderId,
                orderEvent.price,
                orderEvent.quantity,
                orderEvent.quantity - orderEvent.remainedQuantity,
                orderEvent.javaClass.simpleName,
                "agent",
                "127.0.0.1",
                orderEvent.eventDate,
                LocalDateTime.now()
            )
        ).awaitFirstOrNull()
    }

    override suspend fun updateOrder(orderEvent: UpdatedOrderEvent) {
        orderEventRepository.save(
            OrderEventsModel(
                null,
                orderEvent.ouid,
                orderEvent.uuid,
                orderEvent.orderId,
                orderEvent.price,
                orderEvent.quantity,
                orderEvent.quantity - orderEvent.remainedQuantity,
                orderEvent.javaClass.simpleName,
                "agent",
                "127.0.0.1",
                orderEvent.eventDate,
                LocalDateTime.now()
            )
        ).awaitFirstOrNull()
    }

    override suspend fun cancelOrder(orderEvent: CancelOrderEvent) {
        orderEventRepository.save(
            OrderEventsModel(
                null,
                orderEvent.ouid,
                orderEvent.uuid,
                orderEvent.orderId,
                orderEvent.price,
                orderEvent.quantity,
                orderEvent.quantity!! - orderEvent.remainedQuantity,
                orderEvent.javaClass.simpleName,
                "agent",
                "127.0.0.1",
                orderEvent.eventDate,
                LocalDateTime.now()
            )
        ).awaitFirst()
    }
}