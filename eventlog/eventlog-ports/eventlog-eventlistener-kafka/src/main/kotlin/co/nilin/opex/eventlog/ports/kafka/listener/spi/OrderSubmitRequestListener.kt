package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderRequestEvent

interface OrderSubmitRequestListener {
    fun id(): String
    suspend fun onOrder(order: OrderRequestEvent, partition: Int, offset: Long, timestamp: Long)
}