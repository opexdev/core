package co.nilin.opex.api.core.spi

import co.nilin.opex.accountant.core.inout.RichOrder

interface OrderPersister {
    suspend fun save(order: RichOrder)
}