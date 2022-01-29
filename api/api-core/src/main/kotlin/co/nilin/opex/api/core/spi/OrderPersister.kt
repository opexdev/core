package co.nilin.opex.api.core.spi

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichOrderUpdate

interface OrderPersister {

    suspend fun save(order: RichOrder)

    suspend fun update(orderUpdate: RichOrderUpdate)
}