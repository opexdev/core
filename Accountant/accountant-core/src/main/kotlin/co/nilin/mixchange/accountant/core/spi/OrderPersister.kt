package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.accountant.core.model.Order

interface OrderPersister {
    suspend fun load(ouid: String): Order?
    suspend fun save(order: Order): Order
}