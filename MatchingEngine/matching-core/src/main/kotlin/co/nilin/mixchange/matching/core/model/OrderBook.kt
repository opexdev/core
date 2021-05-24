package co.nilin.mixchange.matching.core.model

import co.nilin.mixchange.matching.core.inout.OrderCancelCommand
import co.nilin.mixchange.matching.core.inout.OrderEditCommand
import co.nilin.mixchange.matching.core.inout.OrderCreateCommand

interface OrderBook {
    fun pair(): Pair
    fun startReplayMode()
    fun stopReplayMode()
    fun lastOrder(): Order?
    fun handleNewOrderCommand(orderCommand: OrderCreateCommand): Order?
    fun handleCancelCommand(orderCommand: OrderCancelCommand)
    fun handleEditCommand(orderCommand: OrderEditCommand): Order?
    fun persistent(): PersistentOrderBook
}