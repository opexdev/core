package co.nilin.opex.port.websocket.listener

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.port.websocket.config.AppDispatchers
import co.nilin.opex.port.websocket.kafka.spi.RichOrderListener
import co.nilin.opex.port.websocket.kafka.spi.RichTradeListener
import co.nilin.opex.websocket.core.spi.EventStreamHandler
import kotlinx.coroutines.runBlocking

class WebSocketKafkaListener(private val handler: EventStreamHandler) : RichTradeListener, RichOrderListener {

    override fun id(): String {
        return "WebSocketKafkaListener"
    }

    override fun onTrade(
        trade: RichTrade,
        partition: Int,
        offset: Long,
        timestamp: Long
    ) {
        runBlocking(AppDispatchers.kafkaExecutor) {
            handler.handleTrade(trade)
        }
    }

    override fun onOrder(
        order: RichOrder,
        partition: Int,
        offset: Long,
        timestamp: Long
    ) {
        runBlocking(AppDispatchers.kafkaExecutor) {
            handler.handleOrder(order)
        }
    }
}