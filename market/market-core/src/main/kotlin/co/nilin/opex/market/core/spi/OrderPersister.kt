package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.event.RichOrder
import co.nilin.opex.market.core.event.RichOrderUpdate

interface OrderPersister {

    suspend fun save(order: RichOrder)

    suspend fun update(orderUpdate: RichOrderUpdate)
}