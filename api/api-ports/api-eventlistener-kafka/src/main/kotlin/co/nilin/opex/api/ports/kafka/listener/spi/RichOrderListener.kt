package co.nilin.opex.api.ports.kafka.listener.spi

import co.nilin.opex.api.core.event.RichOrderEvent

interface RichOrderListener {

    fun id(): String

    fun onOrder(order: RichOrderEvent, partition: Int, offset: Long, timestamp: Long)

}