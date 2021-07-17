package co.nilin.mixchange.port.api.kafka.consumer

import co.nilin.mixchange.accountant.core.inout.RichOrder
import co.nilin.mixchange.port.api.kafka.spi.RichOrderListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class OrderKafkaListener() :
        MessageListener<String, RichOrder> {
    val orderListeners = arrayListOf<RichOrderListener>()
    override fun onMessage(data: ConsumerRecord<String, RichOrder>) {
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