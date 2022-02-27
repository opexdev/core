package co.nilin.opex.matching.engine.app.listener

import co.nilin.opex.matching.engine.app.bl.OrderBooks
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.inout.OrderSubmitRequest
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.OrderSubmitRequestListener

class OrderListener : OrderSubmitRequestListener {

    override fun id(): String {
        return "OrderListener"
    }

    override suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long) {
        val orderBook = OrderBooks.lookupOrderBook(
            order.pair.leftSideName + "_"
                    + order.pair.rightSideName
        )
        orderBook.handleNewOrderCommand(
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
    }
}