package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent

interface SwapListener {
    fun id(): String
    fun onEvent(event: SwapEvent, partition: Int, offset: Long, timestamp: Long)
}
