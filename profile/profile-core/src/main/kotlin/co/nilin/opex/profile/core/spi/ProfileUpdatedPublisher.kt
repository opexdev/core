package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.event.ProfileUpdatedEvent


interface ProfileUpdatedPublisher {
    suspend fun publish(event: ProfileUpdatedEvent)
}