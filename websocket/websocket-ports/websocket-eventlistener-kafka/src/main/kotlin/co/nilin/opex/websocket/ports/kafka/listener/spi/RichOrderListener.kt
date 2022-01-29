package co.nilin.opex.websocket.ports.kafka.listener.spi

import co.nilin.opex.accountant.core.inout.RichOrderEvent

interface RichOrderListener {

    fun id(): String

    fun onOrder(order: RichOrderEvent, partition: Int, offset: Long, timestamp: Long)

}