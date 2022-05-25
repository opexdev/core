package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.event.RichOrder
import co.nilin.opex.api.core.event.RichOrderUpdate

interface OrderPersister {

    suspend fun save(order: RichOrder)

    suspend fun update(orderUpdate: RichOrderUpdate)
}