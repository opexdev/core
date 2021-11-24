package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.inout.RichOrder

interface RichOrderPublisher {
    suspend fun publish(order: RichOrder)
}