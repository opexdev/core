package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.accountant.core.inout.OrderStatus
import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichOrderUpdate
import co.nilin.opex.api.core.spi.OrderPersister
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OrderPersisterImpl(
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository
) : OrderPersister {

    private val logger = LoggerFactory.getLogger(OrderPersisterImpl::class.java)

    override suspend fun save(order: RichOrder) {
        orderRepository.save(
            OrderModel(
                null,
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
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        ).awaitFirstOrNull()
        logger.info("order ${order.ouid} saved")

        orderStatusRepository.save(
            OrderStatusModel(
                order.ouid,
                order.executedQuantity.toDouble(),
                order.accumulativeQuoteQty.toDouble(),
                OrderStatus.NEW.code,
                OrderStatus.NEW.orderOfAppearance
            )
        ).awaitFirstOrNull()
        logger.info("OrderStatus ${order.ouid} saved with status of 'NEW'")
    }

    override suspend fun update(orderUpdate: RichOrderUpdate) {
        val executedQuantity = orderUpdate.quantity.minus(orderUpdate.remainedQuantity).toDouble()
        val acc = orderUpdate.price.multiply((orderUpdate.quantity.minus(orderUpdate.remainedQuantity))).toDouble()

        orderStatusRepository.save(
            OrderStatusModel(
                orderUpdate.ouid,
                executedQuantity,
                acc,
                orderUpdate.status.code,
                orderUpdate.status.orderOfAppearance
            )
        ).awaitFirstOrNull()
        logger.info("OrderStatus ${orderUpdate.ouid} updated with status of ${orderUpdate.status}")
    }
}