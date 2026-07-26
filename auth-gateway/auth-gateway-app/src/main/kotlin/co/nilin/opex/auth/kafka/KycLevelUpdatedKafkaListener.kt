package co.nilin.opex.auth.kafka


import co.nilin.opex.auth.data.KycLevelUpdatedEvent
import co.nilin.opex.auth.spi.KycLevelUpdatedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class KycLevelUpdatedKafkaListener : MessageListener<String, KycLevelUpdatedEvent> {
    val eventListeners = arrayListOf<KycLevelUpdatedEventListener>()
    private val logger = LoggerFactory.getLogger(KycLevelUpdatedKafkaListener::class.java)
    override fun onMessage(data: ConsumerRecord<String, KycLevelUpdatedEvent>) {

        eventListeners.forEach { tl ->
            logger.info("incoming new event " + tl.id())
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp(), tl.id())
        }
    }

    fun addEventListener(tl: KycLevelUpdatedEventListener) {
        eventListeners.add(tl)
    }
}