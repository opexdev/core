package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.spi.Listener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener

abstract class EventConsumer<L : Listener<V>, K, V> : MessageListener<K, V> {

    protected val listeners = arrayListOf<L>()

    override fun onMessage(data: ConsumerRecord<K, V>) {
        listeners.forEach { it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp()) }
    }

    fun getListener(id: String): L? {
        return listeners.find { it.id() == id }
    }

    fun countListeners(): Int {
        return listeners.size
    }

    fun addListener(listener: L) {
        listeners.add(listener)
    }

    fun removeListener(listener: L) {
        listeners.removeIf { it.id() == listener.id() }
    }

}