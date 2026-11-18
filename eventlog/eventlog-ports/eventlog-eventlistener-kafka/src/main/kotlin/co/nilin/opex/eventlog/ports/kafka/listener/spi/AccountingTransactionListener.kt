package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent

interface AccountingTransactionListener {
    fun id(): String
    fun onEvent(event: AccountingTransactionKafkaEvent, partition: Int, offset: Long, timestamp: Long)
}
