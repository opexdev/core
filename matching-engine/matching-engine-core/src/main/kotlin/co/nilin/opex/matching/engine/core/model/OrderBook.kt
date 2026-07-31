package co.nilin.opex.matching.engine.core.model

import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand

interface OrderBook {
    fun pair(): Pair
    fun startReplayMode()
    fun stopReplayMode()
    fun lastOrder(): Order?
    suspend fun handleNewOrderCommand(orderCommand: OrderCreateCommand): Order?
    suspend fun handleCancelCommand(orderCommand: OrderCancelCommand)
}