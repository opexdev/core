package co.nilin.opex.websocket.core.spi

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade

interface EventStreamHandler {

    fun handleOrder(order: RichOrder)

    fun handleTrade(trade: RichTrade)

}