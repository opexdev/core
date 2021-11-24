package co.nilin.opex.websocket.ports.kafka.listener.spi

import co.nilin.opex.accountant.core.inout.RichOrder

interface RichOrderListener {
    fun id(): String
    fun onOrder(order: RichOrder, partition: Int, offset: Long, timestamp: Long)
}