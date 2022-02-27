package co.nilin.opex.matching.engine.ports.kafka.listener.spi

import co.nilin.opex.matching.engine.core.inout.OrderSubmitRequest

interface OrderSubmitRequestListener {
    fun id(): String
    suspend fun onOrder(order: OrderSubmitRequest, partition: Int, offset: Long, timestamp: Long)
}