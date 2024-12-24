package co.nilin.opex.market.app.listener

import co.nilin.opex.market.app.config.AppDispatchers
import co.nilin.opex.market.core.event.RichOrder
import co.nilin.opex.market.core.event.RichOrderEvent
import co.nilin.opex.market.core.event.RichOrderUpdate
import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.spi.OrderPersister
import co.nilin.opex.market.core.spi.TradePersister
import co.nilin.opex.market.ports.kafka.listener.spi.RichOrderListener
import co.nilin.opex.market.ports.kafka.listener.spi.RichTradeListener
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class MarketListenerImpl(
    private val richOrderPersister: OrderPersister,
    private val richTradePersister: TradePersister,
    private val meterRegistry: MeterRegistry
) : RichTradeListener, RichOrderListener {
    private val logger = LoggerFactory.getLogger(MarketListenerImpl::class.java)

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
                is RichOrder -> {
                    richOrderPersister.save(order)
                    try {
                        meterRegistry.counter("order_event").increment()
                    } catch (e: Exception) {
                        logger.warn("error in incrementing order_event counter")
                    }
                }

                is RichOrderUpdate -> richOrderPersister.update(order)
            }
        }

    }
}