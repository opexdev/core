package co.nilin.mixchange.api.core.spi

import co.nilin.mixchange.accountant.core.inout.RichOrder

interface OrderPersister {
    suspend fun save(order: RichOrder)
}