package co.nilin.opex.eventlog.app.service

import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class AccountingTransactionKafkaPublisher(
    @Qualifier("eventLogAccountingTransactionKafkaTemplate")
    private val kafkaTemplate: KafkaTemplate<String?, AccountingTransactionKafkaEvent>,
    @Value("\${app.accounting.kafka.publish-topic:accounting_transactions}")
    private val defaultTopic: String
) {
    fun publish(event: AccountingTransactionKafkaEvent, topic: String? = null) {
        kafkaTemplate.send(topic ?: defaultTopic, event.userId, event)
    }
}
