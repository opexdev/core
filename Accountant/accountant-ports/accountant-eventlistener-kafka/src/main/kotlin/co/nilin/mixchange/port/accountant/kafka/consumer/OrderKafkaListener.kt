package co.nilin.mixchange.port.accountant.kafka.consumer

import co.nilin.mixchange.port.accountant.kafka.spi.OrderSubmitRequestListener
import co.nilin.mixchange.port.order.kafka.inout.OrderSubmitRequest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class OrderKafkaListener() :
    MessageListener<String, OrderSubmitRequest> {
    val orderListeners = arrayListOf<OrderSubmitRequestListener>()
    override fun onMessage(data: ConsumerRecord<String, OrderSubmitRequest>) {
        orderListeners.forEach { tl ->
            tl.onOrder(data.value(), data.partition(), data.offset(), data.timestamp())
        }

    }

    fun addOrderListener(tl: OrderSubmitRequestListener) {
        orderListeners.add(tl)
    }

    fun removeOrderListener(tl: OrderSubmitRequestListener) {
        orderListeners.removeIf { item ->
            item.id() == tl.id()
        }
    }
}