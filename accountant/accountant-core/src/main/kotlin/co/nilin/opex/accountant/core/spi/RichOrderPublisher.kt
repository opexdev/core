package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.inout.RichOrderEvent

interface RichOrderPublisher {
    suspend fun publish(order: RichOrderEvent)
}