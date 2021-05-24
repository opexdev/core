package co.nilin.mixchange.eventlog.spi

import co.nilin.mixchange.matching.core.eventh.events.*

interface OrderPersister {
    suspend fun submitOrder(orderEvent: SubmitOrderEvent)
    suspend fun rejectOrder(orderEvent: RejectOrderEvent)
    suspend fun saveOrder(orderEvent: CreateOrderEvent)
    suspend fun updateOrder(orderEvent: UpdatedOrderEvent)
    suspend fun cancelOrder(orderEvent: CancelOrderEvent)
}