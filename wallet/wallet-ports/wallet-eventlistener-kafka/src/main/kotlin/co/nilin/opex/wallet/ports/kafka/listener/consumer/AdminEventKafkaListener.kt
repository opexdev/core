package co.nilin.opex.wallet.ports.kafka.listener.consumer

import co.nilin.opex.wallet.ports.kafka.listener.model.AdminEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.AdminEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class AdminEventKafkaListener : MessageListener<String?, AdminEvent> {

    private val listeners = arrayListOf<AdminEventListener>()

    override fun onMessage(data: ConsumerRecord<String?, AdminEvent>) {
        listeners.forEach {
            it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: AdminEventListener) {
        listeners.add(tl)
    }

    fun removeEventListener(tl: AdminEventListener) {
        listeners.removeIf { it.id() == tl.id() }
    }
}