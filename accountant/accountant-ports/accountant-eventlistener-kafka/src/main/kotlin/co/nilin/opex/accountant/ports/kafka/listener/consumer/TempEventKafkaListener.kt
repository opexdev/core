package co.nilin.opex.accountant.ports.kafka.listener.consumer


import co.nilin.opex.accountant.ports.kafka.listener.spi.TempEventListener
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class TempEventKafkaListener : MessageListener<String, CoreEvent> {

    val eventListeners = arrayListOf<TempEventListener>()

    override fun onMessage(data: ConsumerRecord<String, CoreEvent>) {
        println("TempEvent onMessage")
        eventListeners.forEach { tl ->
            tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: TempEventListener) {
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: TempEventListener) {
        eventListeners.removeIf { item ->
            item.id() == tl.id()
        }
    }
}