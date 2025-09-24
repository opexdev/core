package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.ports.kafka.listener.spi.TempEventListener
import co.nilin.opex.matching.engine.core.eventh.events.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AccountantTempEventListener(
    private val orderManager: OrderManager,
    private val tradeManager: TradeManager
) : TempEventListener {

    private val logger = LoggerFactory.getLogger(AccountantTempEventListener::class.java)

    override fun id(): String {
        return "TempEventListener"
    }

    override fun onEvent(event: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
//        logger.info("TempEvent received $event")
        runBlocking {
            when (event) {
                is CreateOrderEvent -> orderManager.handleNewOrder(event)
                is RejectOrderEvent -> orderManager.handleRejectOrder(event)
                is UpdatedOrderEvent -> orderManager.handleUpdateOrder(event)
                is CancelOrderEvent -> orderManager.handleCancelOrder(event)
                is TradeEvent -> tradeManager.handleTrade(event)
                else -> {
                    throw IllegalArgumentException("Event is not accepted ${event::class.java}")
                }
            }
        }
//        logger.info("TempEvent processed")
    }
}