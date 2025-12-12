package co.nilin.opex.device.ports.kafka.consumer

import co.nilin.opex.device.core.data.LoginEvent
import co.nilin.opex.device.core.spi.LoginEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class LoginKafkaListener : MessageListener<String, LoginEvent> {
    val eventListeners = arrayListOf<LoginEventListener>()
    private val logger = LoggerFactory.getLogger(LoginKafkaListener::class.java)
    override fun onMessage(data: ConsumerRecord<String, LoginEvent>) {

        eventListeners.forEach { tl ->
            logger.info("incoming new event " + tl.id())
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp(), tl.id())
        }
    }

    fun addEventListener(tl: LoginEventListener) {
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: LoginEventListener) {
        eventListeners.removeIf { item ->
            item.id() == tl.id()
        }

    }


}