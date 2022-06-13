package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.spi.Listener
import org.slf4j.LoggerFactory

class ListenerObject : Listener<Any> {

    private val logger = LoggerFactory.getLogger(ListenerObject::class.java)

    override fun id(): String {
        return "AnyListener"
    }

    override fun onEvent(event: Any, partition: Int, offset: Long, timestamp: Long) {
        logger.info("event called")
    }
}