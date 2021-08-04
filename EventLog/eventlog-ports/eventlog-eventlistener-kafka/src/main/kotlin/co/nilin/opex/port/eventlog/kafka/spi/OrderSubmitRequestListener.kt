package co.nilin.opex.port.eventlog.kafka.spi

import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest

interface OrderSubmitRequestListener {
    fun id(): String
    suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long)
}