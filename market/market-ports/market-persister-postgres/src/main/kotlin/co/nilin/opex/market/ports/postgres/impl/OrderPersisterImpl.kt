package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.common.utils.justTry
import co.nilin.opex.market.core.event.RichOrder
import co.nilin.opex.market.core.event.RichOrderUpdate
import co.nilin.opex.market.core.inout.Order
import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.core.spi.OrderPersister
import co.nilin.opex.market.ports.postgres.dao.OpenOrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.model.OrderModel
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import co.nilin.opex.market.ports.postgres.util.asOrderDTO
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OrderPersisterImpl(
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val openOrderRepository: OpenOrderRepository,
    private val redisCacheHelper: RedisCacheHelper
) : OrderPersister {

    private val logger = LoggerFactory.getLogger(OrderPersisterImpl::class.java)

    @Transactional
    override suspend fun save(order: RichOrder) {
        val orderModel = OrderModel(
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
        orderRepository.save(orderModel).awaitFirstOrNull()
        logger.info("order ${order.ouid} saved")

        orderStatusRepository.insert(
            order.ouid,
            order.executedQuantity,
            order.accumulativeQuoteQty,
            OrderStatus.NEW.code,
            OrderStatus.NEW.orderOfAppearance
        ).awaitFirstOrNull()
        logger.info("OrderStatus ${order.ouid} saved with status of 'NEW'")

        val lastStatus = orderStatusRepository.findMostRecentByOUID(order.ouid).awaitSingle()
        if (OrderStatus.fromCode(lastStatus.status)!!.isOpenOrder()) {
            openOrderRepository.insertOrUpdate(order.ouid, lastStatus.executedQuantity, lastStatus.status)
                .awaitFirstOrNull()
            logger.info("Order ${order.ouid} added to open orders")
        } else {
            openOrderRepository.delete(order.ouid).awaitSingleOrNull()
            logger.info("Order ${order.ouid} deleted from open orders")
        }

        justTry { redisCacheHelper.put("lastOrder", orderModel.asOrderDTO(lastStatus)) }
    }

    @Transactional
    override suspend fun update(orderUpdate: RichOrderUpdate) {
        orderStatusRepository.insert(
            orderUpdate.ouid,
            orderUpdate.executedQuantity(),
            orderUpdate.accumulativeQuoteQuantity(),
            orderUpdate.status.code,
            orderUpdate.status.orderOfAppearance
        ).awaitFirstOrNull()
        logger.info("OrderStatus ${orderUpdate.ouid} updated with status of ${orderUpdate.status}")

        val lastStatus = orderStatusRepository.findMostRecentByOUID(orderUpdate.ouid).awaitSingle()
        if (OrderStatus.fromCode(lastStatus.status)!!.isOpenOrder()) {
            openOrderRepository.insertOrUpdate(orderUpdate.ouid, lastStatus.executedQuantity, lastStatus.status)
                .awaitFirstOrNull()
            logger.info("Order ${orderUpdate.ouid} added to open orders")
        } else {
            openOrderRepository.delete(orderUpdate.ouid).awaitSingleOrNull()
            logger.info("Order ${orderUpdate.ouid} deleted from open orders")
        }
    }

    override suspend fun load(ouid: String): Order? {
        return orderRepository.findByOuid(ouid)
            .awaitFirstOrNull()
            ?.asOrderDTO(orderStatusRepository.findMostRecentByOUID(ouid).awaitSingleOrNull())
    }
}