package co.nilin.opex.matching.engine.app.listener

import co.nilin.opex.matching.engine.app.bl.OrderBooks
import co.nilin.opex.matching.engine.core.inout.*
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.OrderRequestEventListener
import org.slf4j.LoggerFactory

class OrderListener : OrderRequestEventListener {

    private val logger = LoggerFactory.getLogger(OrderListener::class.java)

    override fun id(): String {
        return "OrderListener"
    }

    override suspend fun onOrder(order: OrderRequestEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("OrderRequestEvent received. ${order::class.java.simpleName} ouid=${order.ouid}")
        val orderBook = OrderBooks.lookupOrderBook(
            order.pair.leftSideName + "_"
                    + order.pair.rightSideName
        )

        when (order) {
            is OrderSubmitRequestEvent -> orderBook.handleNewOrderCommand(
                OrderCreateCommand(
                    order.ouid,
                    order.uuid,
                    order.pair,
                    order.price,
                    order.quantity,
                    order.direction,
                    order.matchConstraint,
                    order.orderType
                )
            )

            is OrderCancelRequestEvent -> orderBook.handleCancelCommand(
                OrderCancelCommand(
                    order.ouid,
                    order.uuid,
                    order.orderId,
                    order.pair
                )
            )

            else -> logger.warn("Unknown event type of OrderRequestEvent")
        }
    }
}