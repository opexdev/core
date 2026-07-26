package co.nilin.opex.device.ports.kafka.consumer

import co.nilin.opex.device.core.data.LoginEvent
import co.nilin.opex.device.core.data.LogoutEvent
import co.nilin.opex.device.core.spi.LoginEventListener
import co.nilin.opex.device.core.spi.LogoutEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class LogoutKafkaListener : MessageListener<String, LogoutEvent> {
    val eventListeners = arrayListOf<LogoutEventListener>()
    private val logger = LoggerFactory.getLogger(LogoutKafkaListener::class.java)
    override fun onMessage(data: ConsumerRecord<String, LogoutEvent>) {

        eventListeners.forEach { tl ->
            logger.info("incoming new event " + tl.id())
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp(), tl.id())
        }
    }

    fun addEventListener(tl: LogoutEventListener) {
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: LogoutEventListener) {
        eventListeners.removeIf { item ->
            item.id() == tl.id()
        }

    }


}