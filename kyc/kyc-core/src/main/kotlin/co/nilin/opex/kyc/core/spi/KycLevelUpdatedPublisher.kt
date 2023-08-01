package co.nilin.opex.kyc.core.spi

import co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent

interface KycLevelUpdatedPublisher {
    suspend fun publish(order: KycLevelUpdatedEvent)
}