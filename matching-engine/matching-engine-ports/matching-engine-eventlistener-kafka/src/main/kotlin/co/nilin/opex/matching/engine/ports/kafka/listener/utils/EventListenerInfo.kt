package co.nilin.opex.matching.engine.ports.kafka.listener.utils

import org.springframework.stereotype.Component
import java.util.*

@Component
class EventListenerInfo {

    final var lastProcessedOrderRequestEventTime: Long? = null
        private set

    fun updateLastProcessedOrderRequestEvent() {
        lastProcessedOrderRequestEventTime = Date().time
    }
}