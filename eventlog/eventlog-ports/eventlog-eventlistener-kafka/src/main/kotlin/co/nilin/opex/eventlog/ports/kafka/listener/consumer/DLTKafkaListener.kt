package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.spi.DLTListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class DLTKafkaListener : MessageListener<String?, String> {

    private val listeners = arrayListOf<DLTListener>()

    override fun onMessage(data: ConsumerRecord<String?, String>) {

        listeners.forEach { it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp(), data.headers()) }
    }

    fun addEventListener(tl: DLTListener) {
        listeners.add(tl)
    }

    fun removeEventListener(tl: DLTListener) {
        listeners.removeIf {
            it.id() == tl.id()
        }
    }
}