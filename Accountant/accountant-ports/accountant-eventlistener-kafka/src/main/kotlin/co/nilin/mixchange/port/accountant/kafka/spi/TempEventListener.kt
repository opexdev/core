package co.nilin.mixchange.port.trade.spi

import co.nilin.mixchange.matching.core.eventh.events.CoreEvent


interface TempEventListener {
    fun id(): String
    fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long)
}