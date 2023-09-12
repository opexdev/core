package co.nilin.opex.matching.engine.ports.kafka.listener.spi

import co.nilin.opex.matching.engine.core.inout.OrderRequestEvent

interface OrderRequestEventListener {
    fun id(): String
    suspend fun onOrder(order: OrderRequestEvent, partition: Int, offset: Long, timestamp: Long)
}