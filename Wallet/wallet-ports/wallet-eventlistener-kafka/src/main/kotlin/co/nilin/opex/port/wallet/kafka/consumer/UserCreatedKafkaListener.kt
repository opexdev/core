package co.nilin.opex.port.wallet.kafka.consumer


import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.port.wallet.kafka.spi.UserCreatedEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class UserCreatedKafkaListener: MessageListener<String, UserCreatedEvent> {
    val eventListeners = arrayListOf<UserCreatedEventListener>()

    override fun onMessage(data: ConsumerRecord<String, UserCreatedEvent>) {
        eventListeners.forEach{
            tl -> tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: UserCreatedEventListener){
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: UserCreatedEventListener){
        eventListeners.removeIf {
            item -> item.id() == tl.id()
        }
    }
}