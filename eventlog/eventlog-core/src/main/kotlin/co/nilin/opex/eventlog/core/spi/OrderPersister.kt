package co.nilin.opex.eventlog.core.spi

import co.nilin.opex.matching.engine.core.eventh.events.*

interface OrderPersister {
    suspend fun submitOrder(orderEvent: SubmitOrderEvent)
    suspend fun rejectOrder(orderEvent: RejectOrderEvent)
    suspend fun saveOrder(orderEvent: CreateOrderEvent)
    suspend fun updateOrder(orderEvent: UpdatedOrderEvent)
    suspend fun cancelOrder(orderEvent: CancelOrderEvent)
}