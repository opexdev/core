package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.DepositListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class DepositKafkaListener : MessageListener<String, DepositEvent> {
    private val listeners = arrayListOf<DepositListener>()

    override fun onMessage(data: ConsumerRecord<String, DepositEvent>) {
        listeners.forEach {
            it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addListener(listener: DepositListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DepositListener) {
        listeners.removeIf { it.id() == listener.id() }
    }
}
