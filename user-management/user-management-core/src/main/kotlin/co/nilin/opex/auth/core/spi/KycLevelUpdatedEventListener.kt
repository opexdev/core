package co.nilin.opex.auth.core.spi

import co.nilin.opex.auth.core.data.KycLevelUpdatedEvent


interface KycLevelUpdatedEventListener {
    fun id(): String
     fun onEvent(event: KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId:String)

}