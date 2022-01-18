package co.nilin.opex.websocket.app.listener

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.inout.RichOrderUpdate
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.websocket.ports.kafka.listener.spi.RichOrderListener
import co.nilin.opex.websocket.ports.kafka.listener.spi.RichTradeListener
import co.nilin.opex.websocket.core.spi.EventStreamHandler

class WebSocketKafkaListener(private val handler: EventStreamHandler) : RichTradeListener, RichOrderListener {

    override fun id(): String {
        return "WebSocketKafkaListener"
    }

    override fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long) {
        handler.handleTrade(trade)
    }

    override fun onOrder(order: RichOrderEvent, partition: Int, offset: Long, timestamp: Long) {
        when (order) {
            is RichOrder -> handler.handleOrder(order)
            is RichOrderUpdate -> handler.handleOrderUpdate(order)
        }
    }
}