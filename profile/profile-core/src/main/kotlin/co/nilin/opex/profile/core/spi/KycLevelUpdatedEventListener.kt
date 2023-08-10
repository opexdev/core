package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent


interface KycLevelUpdatedEventListener {
    fun id(): String
     fun onEvent(event: KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId:String)

}