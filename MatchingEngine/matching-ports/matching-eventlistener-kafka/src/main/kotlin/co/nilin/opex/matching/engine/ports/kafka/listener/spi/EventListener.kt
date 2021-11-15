package co.nilin.opex.matching.engine.ports.kafka.listener.spi

import co.nilin.opex.matching.core.eventh.events.CoreEvent

interface EventListener {
    fun id(): String
    fun onEvent(event: CoreEvent, partition: Int, offset: Long, timestamp: Long)
}