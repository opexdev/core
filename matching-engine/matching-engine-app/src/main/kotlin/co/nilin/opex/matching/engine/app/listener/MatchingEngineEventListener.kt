package co.nilin.opex.matching.engine.app.listener

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.EventListener
import org.slf4j.LoggerFactory

class MatchingEngineEventListener : EventListener {

    private val logger = LoggerFactory.getLogger(MatchingEngineEventListener::class.java)

    override fun id(): String {
        return "EventListener"
    }

    override fun onEvent(event: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("Received CoreEvent: ${event::class.java}")
    }
}