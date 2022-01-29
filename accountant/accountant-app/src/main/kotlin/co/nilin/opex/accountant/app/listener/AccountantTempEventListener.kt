package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.core.api.TradeManager
import co.nilin.opex.accountant.ports.kafka.listener.spi.TempEventListener
import co.nilin.opex.matching.engine.core.eventh.events.*
import kotlinx.coroutines.runBlocking

class AccountantTempEventListener(
    private val orderManager: OrderManager,
    private val tradeManager: TradeManager
) : TempEventListener {

    override fun id(): String {
        return "TempEventListener"
    }

    override fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
        println("TempEvent $coreEvent")
        runBlocking {
            when (coreEvent) {
                is CreateOrderEvent -> orderManager.handleNewOrder(coreEvent)
                is RejectOrderEvent -> orderManager.handleRejectOrder(coreEvent)
                is UpdatedOrderEvent -> orderManager.handleUpdateOrder(coreEvent)
                is CancelOrderEvent -> orderManager.handleCancelOrder(coreEvent)
                is TradeEvent -> tradeManager.handleTrade(coreEvent)
                else -> {
                    throw IllegalArgumentException("Event is not accepted ${coreEvent::class.java}")
                }
            }
        }
        println("onEvent")
    }
}