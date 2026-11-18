package co.nilin.opex.eventlog.ports.kafka.listener.spi

import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent

interface WithdrawRequestListener {
    fun id(): String
    fun onEvent(event: WithdrawRequestEvent, partition: Int, offset: Long, timestamp: Long)
}
