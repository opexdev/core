package co.nilin.opex.matching.gateway.ports.kafka.submitter.utils

import org.springframework.stereotype.Component
import java.util.*

@Component
class EventSubmitterInfo {

    final var lastSentOrderRequestTime: Long? = null
        private set

    internal fun updateLastProcessedOrderRequestTime() {
        lastSentOrderRequestTime = Date().time
    }
}