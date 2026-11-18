package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.spi.PublishedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class PublishedEventKafkaListener : MessageListener<String, String> {
    private val listeners = arrayListOf<PublishedEventListener>()

    override fun onMessage(data: ConsumerRecord<String, String>) {
        listeners.forEach {
            it.onEvent(
                topic = data.topic(),
                key = data.key(),
                payload = data.value(),
                partition = data.partition(),
                offset = data.offset(),
                timestamp = data.timestamp()
            )
        }
    }

    fun addListener(listener: PublishedEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PublishedEventListener) {
        listeners.removeIf { it.id() == listener.id() }
    }
}
