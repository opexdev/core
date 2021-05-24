package co.nilin.mixchange.port.accountant.kafka.spi

import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitRequest

interface OrderSubmitRequestListener {
    fun id(): String
    fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long)
}