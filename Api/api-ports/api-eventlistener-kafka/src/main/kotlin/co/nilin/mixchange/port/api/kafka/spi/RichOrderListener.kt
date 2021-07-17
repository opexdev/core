package co.nilin.mixchange.port.api.kafka.spi

import co.nilin.mixchange.accountant.core.inout.RichOrder

interface RichOrderListener {
    fun id(): String
    fun onOrder(order: RichOrder, partition: Int, offset: Long, timestamp: Long)
}