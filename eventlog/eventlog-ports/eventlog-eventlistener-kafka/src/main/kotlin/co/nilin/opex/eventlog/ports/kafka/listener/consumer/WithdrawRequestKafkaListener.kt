package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.WithdrawRequestListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class WithdrawRequestKafkaListener : MessageListener<String, WithdrawRequestEvent> {
    private val listeners = arrayListOf<WithdrawRequestListener>()

    override fun onMessage(data: ConsumerRecord<String, WithdrawRequestEvent>) {
        listeners.forEach {
            it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addListener(listener: WithdrawRequestListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: WithdrawRequestListener) {
        listeners.removeIf { it.id() == listener.id() }
    }
}
