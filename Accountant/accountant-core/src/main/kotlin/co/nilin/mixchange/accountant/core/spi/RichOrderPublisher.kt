package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.accountant.core.inout.RichOrder

interface RichOrderPublisher {
    suspend fun publish(order: RichOrder)
}