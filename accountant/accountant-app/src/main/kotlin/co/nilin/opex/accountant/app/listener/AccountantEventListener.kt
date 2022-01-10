package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.api.OrderManager
import co.nilin.opex.accountant.ports.kafka.listener.spi.EventListener
import co.nilin.opex.matching.engine.core.eventh.events.*
import kotlinx.coroutines.runBlocking

class AccountantEventListener(private val orderManager: OrderManager) : EventListener {

    override fun id(): String {
        return "EventListener"
    }

    override fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            when (coreEvent) {
                is CreateOrderEvent -> orderManager.handleNewOrder(coreEvent)
                is RejectOrderEvent -> orderManager.handleRejectOrder(coreEvent)
                is UpdatedOrderEvent -> orderManager.handleUpdateOrder(coreEvent)
                is CancelOrderEvent -> orderManager.handleCancelOrder(coreEvent)
                else -> {
                    println("Event is not accepted ${coreEvent::class.java}")
                }
            }
        }
        println("onEvent")
    }
}