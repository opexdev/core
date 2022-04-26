package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.spi.OrderPersister
import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderSubmitRequest
import co.nilin.opex.eventlog.ports.kafka.listener.spi.OrderSubmitRequestListener
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent

class OrderListener(private val orderPersister: OrderPersister) : OrderSubmitRequestListener {

    override fun id(): String {
        return "OrderListener"
    }

    override suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
        orderPersister.submitOrder(
            SubmitOrderEvent(
                order.ouid,
                order.uuid,
                order.orderId,
                order.pair,
                order.price,
                order.quantity,
                0,
                order.direction,
                order.matchConstraint,
                order.orderType
            )
        )
    }
}