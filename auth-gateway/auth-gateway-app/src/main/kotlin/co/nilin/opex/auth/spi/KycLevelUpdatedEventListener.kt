package co.nilin.opex.auth.spi

import co.nilin.opex.auth.data.KycLevelUpdatedEvent


interface KycLevelUpdatedEventListener {
    fun id(): String
    fun onEvent(event: KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String)

}