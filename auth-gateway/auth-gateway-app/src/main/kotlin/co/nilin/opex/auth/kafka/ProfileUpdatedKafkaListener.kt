package co.nilin.opex.auth.kafka


import co.nilin.opex.auth.data.ProfileUpdatedEvent
import co.nilin.opex.auth.spi.ProfileUpdatedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class ProfileUpdatedKafkaListener : MessageListener<String, ProfileUpdatedEvent> {
    val eventListeners = arrayListOf<ProfileUpdatedEventListener>()
    private val logger = LoggerFactory.getLogger(ProfileUpdatedKafkaListener::class.java)
    override fun onMessage(data: ConsumerRecord<String, ProfileUpdatedEvent>) {

        eventListeners.forEach { tl ->
            logger.info("incoming new event " + tl.id())
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp(), tl.id())
        }
    }

    fun addEventListener(tl: ProfileUpdatedEventListener) {
        eventListeners.add(tl)
    }
}