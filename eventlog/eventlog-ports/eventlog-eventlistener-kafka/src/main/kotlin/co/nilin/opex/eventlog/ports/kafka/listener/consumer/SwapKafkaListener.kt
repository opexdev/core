package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.SwapListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class SwapKafkaListener : MessageListener<String, SwapEvent> {
    private val listeners = arrayListOf<SwapListener>()

    override fun onMessage(data: ConsumerRecord<String, SwapEvent>) {
        listeners.forEach {
            it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addListener(listener: SwapListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SwapListener) {
        listeners.removeIf { it.id() == listener.id() }
    }
}
