package co.nilin.opex.port.eventlog.kafka.consumer

import co.nilin.opex.port.eventlog.kafka.spi.OrderSubmitRequestListener
import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener


class OrderKafkaListener(private val executorCoroutineDispatcher: ExecutorCoroutineDispatcher) :
    MessageListener<String, OrderSubmitRequest> {
    val orderListeners = arrayListOf<OrderSubmitRequestListener>()
    override fun onMessage(data: ConsumerRecord<String, OrderSubmitRequest>) {
        runBlocking {
            orderListeners.forEach { tl ->
                withContext(executorCoroutineDispatcher) {
                    tl.onOrder(data.value(), data.partition(), data.offset(), data.timestamp())
                }
            }
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