package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.accountant.ports.kafka.listener.inout.OrderSubmitRequest

interface OrderSubmitRequestListener {
    fun id(): String
    fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long)
}