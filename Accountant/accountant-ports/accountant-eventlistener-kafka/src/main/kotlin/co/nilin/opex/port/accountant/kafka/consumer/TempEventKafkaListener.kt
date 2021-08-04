package co.nilin.opex.port.accountant.kafka.consumer


import co.nilin.opex.matching.core.eventh.events.CoreEvent
import co.nilin.opex.port.accountant.kafka.spi.TempEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class TempEventKafkaListener: MessageListener<String, CoreEvent> {

    val eventListeners = arrayListOf<TempEventListener>()

    override fun onMessage(data: ConsumerRecord<String, CoreEvent>) {
        println("TempEvent onMessage")
        eventListeners.forEach{
            tl -> tl.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addEventListener(tl: TempEventListener){
        eventListeners.add(tl)
    }

    fun removeEventListener(tl: TempEventListener){
        eventListeners.removeIf {
            item -> item.id() == tl.id()
        }
    }
}