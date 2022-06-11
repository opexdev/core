package co.nilin.opex.market.ports.kafka.listener.spi

import co.nilin.opex.market.core.event.RichOrderEvent

interface RichOrderListener {

    fun id(): String

    fun onOrder(order: RichOrderEvent, partition: Int, offset: Long, timestamp: Long)

}