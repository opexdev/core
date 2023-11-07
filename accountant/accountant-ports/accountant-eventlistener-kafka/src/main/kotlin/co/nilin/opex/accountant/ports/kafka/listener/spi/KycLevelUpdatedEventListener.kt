package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.accountant.core.inout.KycLevelUpdatedEvent


interface KycLevelUpdatedEventListener {
    fun id(): String
    fun onEvent(event: KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String)

}