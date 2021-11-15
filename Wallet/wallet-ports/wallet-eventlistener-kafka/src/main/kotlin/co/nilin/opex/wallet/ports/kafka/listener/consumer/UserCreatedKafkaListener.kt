package co.nilin.opex.wallet.ports.kafka.listener.consumer


import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.UserCreatedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class UserCreatedKafkaListener : MessageListener<String, UserCreatedEvent> {
    val eventListeners = arrayListOf<UserCreatedEventListener>()

    override fun onMessage(data: ConsumerRecord<String, UserCreatedEvent>) {
        eventListeners.forEach { tl ->
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: UserCreatedEventListener) {
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: UserCreatedEventListener) {
        eventListeners.removeIf { item ->
            item.id() == tl.id()
        }
    }
}