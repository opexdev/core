package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderSubmitRequest

interface OrderSubmitRequestListener {
    fun id(): String
    suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long)
}