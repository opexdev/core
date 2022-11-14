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

    override fun onEvent(event: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            logger.info("Order submit event received ${event.ouid}")

            orderManager.handleRequestOrder(
                SubmitOrderEvent(
                    event.ouid,
                    event.uuid,
                    event.orderId,
                    event.pair,
                    event.price,
                    event.quantity,
                    event.quantity,
                    event.direction,
                    event.matchConstraint,
                    event.orderType,
                    event.userLevel
                )
            )
        }
    }
}