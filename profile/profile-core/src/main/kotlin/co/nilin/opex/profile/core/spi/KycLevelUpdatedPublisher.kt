package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent


interface KycLevelUpdatedPublisher {
    suspend fun publish(event: KycLevelUpdatedEvent)

}