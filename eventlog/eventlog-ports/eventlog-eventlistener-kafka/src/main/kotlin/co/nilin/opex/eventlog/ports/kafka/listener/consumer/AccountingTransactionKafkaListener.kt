package co.nilin.opex.eventlog.ports.kafka.listener.consumer

import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.AccountingTransactionListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class AccountingTransactionKafkaListener : MessageListener<String, AccountingTransactionKafkaEvent> {
    private val listeners = arrayListOf<AccountingTransactionListener>()

    override fun onMessage(data: ConsumerRecord<String, AccountingTransactionKafkaEvent>) {
        listeners.forEach {
            it.onEvent(data.value(), data.partition(), data.offset(), data.timestamp())
        }
    }

    fun addListener(listener: AccountingTransactionListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: AccountingTransactionListener) {
        listeners.removeIf { it.id() == listener.id() }
    }
}
