package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

interface EventListener {
    fun id(): String
    fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long)
}