package co.nilin.opex.profile.core.spi
import co.nilin.opex.profile.core.data.profile.UserCreatedEvent


interface KycLevelUpdatedEventListener {
    fun id(): String
    fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long)
    fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, eventId:String)

}