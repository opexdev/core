package co.nilin.opex.auth.spi

import co.nilin.opex.auth.data.ProfileUpdatedEvent


interface ProfileUpdatedEventListener {
    fun id(): String
    fun onEvent(event: ProfileUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String)

}