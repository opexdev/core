package co.nilin.opex.api.app.listener

import co.nilin.opex.api.app.config.AppDispatchers
import co.nilin.opex.api.core.event.RichOrder
import co.nilin.opex.api.core.event.RichOrderEvent
import co.nilin.opex.api.core.event.RichOrderUpdate
import co.nilin.opex.api.core.event.RichTrade
import co.nilin.opex.api.core.spi.OrderPersister
import co.nilin.opex.api.core.spi.TradePersister
import co.nilin.opex.api.ports.kafka.listener.spi.RichOrderListener
import co.nilin.opex.api.ports.kafka.listener.spi.RichTradeListener
import kotlinx.coroutines.runBlocking

class ApiListenerImpl(
    private val richOrderPersister: OrderPersister,
    private val richTradePersister: TradePersister
) : RichTradeListener, RichOrderListener {

    override fun id(): String {
        return "AppListener"
    }

    override fun onTrade(trade: RichTrade, partition: Int, offset: Long, timestamp: Long) {
        println("RichTrade received")
        runBlocking(AppDispatchers.kafkaExecutor) {
            richTradePersister.save(trade)
        }
    }

    override fun onOrder(order: RichOrderEvent, partition: Int, offset: Long, timestamp: Long) {
        runBlocking(AppDispatchers.kafkaExecutor) {
            when (order) {
                is RichOrder -> richOrderPersister.save(order)
                is RichOrderUpdate -> richOrderPersister.update(order)
            }
        }
    }
}