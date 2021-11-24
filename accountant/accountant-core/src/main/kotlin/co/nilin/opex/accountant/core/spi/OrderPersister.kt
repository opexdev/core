package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.Order

interface OrderPersister {
    suspend fun load(ouid: String): Order?
    suspend fun save(order: Order): Order
}