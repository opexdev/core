package co.nilin.opex.websocket.ports.kafka.listener.consumer

import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.websocket.ports.kafka.listener.spi.RichOrderListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class OrderKafkaListener : MessageListener<String, RichOrderEvent> {

    val orderListeners = arrayListOf<RichOrderListener>()

    override fun onMessage(data: ConsumerRecord<String, RichOrderEvent>) {
        orderListeners.forEach { tl ->
            tl.onOrder(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addOrderListener(tl: RichOrderListener) {
        orderListeners.add(tl)
    }

    fun removeOrderListener(tl: RichOrderListener) {
        orderListeners.removeIf { item ->
            item.id() == tl.id()
        }
    }
}