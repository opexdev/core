package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.spi.OrderPersister
import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderSubmitRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.OrderSubmitRequestListener
import co.nilin.opex.matching.engine.core.eventh.events.SubmitOrderEvent

class OrderListener(private val orderPersister: OrderPersister) : OrderSubmitRequestListener {

    override fun id(): String {
        return "OrderListener"
    }

    override suspend fun onOrder(order: OrderRequestEvent, partition: Int, offset: Long, timestamp: Long) {
        if (order is OrderSubmitRequestEvent)
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