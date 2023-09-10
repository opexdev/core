package co.nilin.opex.kyc.core.spi

import co.nilin.opex.kyc.core.data.event.UserCreatedEvent


interface UserCreatedEventListener {
    fun id(): String
    fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String)

}