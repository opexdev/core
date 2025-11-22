package co.nilin.opex.wallet.ports.kafka.listener.consumer

import co.nilin.opex.wallet.ports.kafka.listener.model.ProfileUpdatedEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.ProfileUpdatedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class ProfileUpdatedKafkaListener : MessageListener<String?, ProfileUpdatedEvent> {

    private val listeners = arrayListOf<ProfileUpdatedEventListener>()

    override fun onMessage(data: ConsumerRecord<String?, ProfileUpdatedEvent>) {
        listeners.forEach {
            it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: ProfileUpdatedEventListener) {
        listeners.add(tl)
    }

    fun removeEventListener(tl: ProfileUpdatedEventListener) {
        listeners.removeIf { it.id() == tl.id() }
    }
}