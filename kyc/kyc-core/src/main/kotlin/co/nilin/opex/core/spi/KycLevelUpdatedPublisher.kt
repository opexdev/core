package co.nilin.opex.core.spi

import co.nilin.opex.core.event.KycLevelUpdatedEvent

interface KycLevelUpdatedPublisher {
    suspend fun publish(order: KycLevelUpdatedEvent)
}