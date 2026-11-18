package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent

interface DepositListener {
    fun id(): String
    fun onEvent(event: DepositEvent, partition: Int, offset: Long, timestamp: Long)
}
