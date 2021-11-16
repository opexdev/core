package co.nilin.opex.port.websocket.listener

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.port.websocket.config.AppDispatchers
import co.nilin.opex.port.websocket.kafka.spi.RichOrderListener
import co.nilin.opex.port.websocket.kafka.spi.RichTradeListener
import kotlinx.coroutines.runBlocking

class WebSocketKafkaListener : RichTradeListener, RichOrderListener {

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
            //TODO send to user
        }
    }

    override fun onOrder(
        order: RichOrder,
        partition: Int,
        offset: Long,
        timestamp: Long
    ) {
        runBlocking(AppDispatchers.kafkaExecutor) {
            //TODO send to user
        }
    }
}