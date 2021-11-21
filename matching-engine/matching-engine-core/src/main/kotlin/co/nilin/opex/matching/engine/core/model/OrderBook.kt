package co.nilin.opex.matching.engine.core.model

import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.inout.OrderEditCommand

interface OrderBook {
    fun pair(): Pair
    fun startReplayMode()
    fun stopReplayMode()
    fun lastOrder(): Order?
    fun handleNewOrderCommand(orderCommand: OrderCreateCommand): Order?
    fun handleCancelCommand(orderCommand: OrderCancelCommand)
    fun handleEditCommand(orderCommand: OrderEditCommand): Order?
}