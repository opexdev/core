package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.event.RichOrder
import co.nilin.opex.market.core.event.RichOrderUpdate
import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.core.spi.OrderPersister
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.model.OrderModel
import co.nilin.opex.market.ports.postgres.model.OrderStatusModel
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
                order.makerFee,
                order.takerFee,
                order.leftSideFraction,
                order.rightSideFraction,
                order.userLevel,
                order.direction,
                order.constraint,
                order.type,
                order.price,
                order.quantity,
                order.quoteQuantity,
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        ).awaitFirstOrNull()
        logger.info("order ${order.ouid} saved")

        orderStatusRepository.save(
            OrderStatusModel(
                order.ouid,
                order.executedQuantity,
                order.accumulativeQuoteQty,
                OrderStatus.NEW.code,
                OrderStatus.NEW.orderOfAppearance
            )
        ).awaitFirstOrNull()
        logger.info("OrderStatus ${order.ouid} saved with status of 'NEW'")
    }

    override suspend fun update(orderUpdate: RichOrderUpdate) {
        try {
            orderStatusRepository.save(
                OrderStatusModel(
                    orderUpdate.ouid,
                    orderUpdate.executedQuantity(),
                    orderUpdate.accumulativeQuoteQuantity(),
                    orderUpdate.status.code,
                    orderUpdate.status.orderOfAppearance
                )
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Error updating order status: ${e.message}")
        }
        logger.info("OrderStatus ${orderUpdate.ouid} updated with status of ${orderUpdate.status}")
    }

    override suspend fun load(ouid: String) {
        orderRepository.findByOuid(ouid)
    }
}