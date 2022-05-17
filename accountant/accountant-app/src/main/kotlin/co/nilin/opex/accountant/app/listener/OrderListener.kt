package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.ports.kafka.listener.inout.OrderSubmitRequest
import co.nilin.opex.accountant.ports.kafka.listener.spi.OrderSubmitRequestListener
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class OrderListener(private val orderManager: OrderManager) : OrderSubmitRequestListener {

    private val logger = LoggerFactory.getLogger(OrderListener::class.java)

    override fun id(): String {
        return "OrderListener"
    }

    override fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            logger.info("Order submit event received ${order.ouid}")

            orderManager.handleRequestOrder(
                SubmitOrderEvent(
                    order.ouid,
                    order.uuid,
                    order.orderId,
                    order.pair,
                    order.price,
                    order.quantity,
                    order.quantity,
                    order.direction,
                    order.matchConstraint,
                    order.orderType
                )
            )
        }
    }
}