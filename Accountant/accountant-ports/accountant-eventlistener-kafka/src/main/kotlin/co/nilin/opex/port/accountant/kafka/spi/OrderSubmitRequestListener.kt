package co.nilin.opex.port.accountant.kafka.spi

import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest

interface OrderSubmitRequestListener {
    fun id(): String
    fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long)
}