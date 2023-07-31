package co.nilin.opex.profile.ports.kafka.consumer

import co.nilin.opex.profile.core.data.profile.UserCreatedEvent
import co.nilin.opex.profile.core.spi.KycLevelUpdatedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class KycLevelUpdatedKafkaListener  : MessageListener<String, UserCreatedEvent> {
    val eventListeners = arrayListOf<KycLevelUpdatedEventListener>()
    private val logger = LoggerFactory.getLogger(KycLevelUpdatedKafkaListener::class.java)
    override fun onMessage(data: ConsumerRecord<String, UserCreatedEvent>) {

        eventListeners.forEach { tl ->
            logger.info("incoming new event "+tl.id())
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: KycLevelUpdatedEventListener) {
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: KycLevelUpdatedEventListener) {
        eventListeners.removeIf { item ->
            item.id() == tl.id()
        }

    }

}