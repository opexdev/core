package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.OrderSubmitRequestListener
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener


class OrderKafkaListener(private val executorCoroutineDispatcher: ExecutorCoroutineDispatcher) :
    MessageListener<String, OrderRequestEvent> {

    private val orderListeners = arrayListOf<OrderSubmitRequestListener>()

    override fun onMessage(data: ConsumerRecord<String, OrderRequestEvent>) {
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