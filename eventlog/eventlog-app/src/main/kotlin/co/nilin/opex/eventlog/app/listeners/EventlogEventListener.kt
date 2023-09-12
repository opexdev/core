package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.spi.EventPersister
import co.nilin.opex.eventlog.core.spi.OrderPersister
import co.nilin.opex.eventlog.ports.kafka.listener.spi.EventListener
import co.nilin.opex.matching.engine.core.eventh.events.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogEventListener(
    private val orderPersister: OrderPersister,
    private val eventPersister: EventPersister
) : EventListener {

    private val log = LoggerFactory.getLogger(EventlogEventListener::class.java)

    override fun id(): String {
        return "EventListener"
    }

    override fun onEvent(coreEvent: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
        log.info("Receive CoreEvent $coreEvent")
        runBlocking {
            when (coreEvent) {
                is CreateOrderEvent -> orderPersister.saveOrder(coreEvent)
                is RejectOrderEvent -> orderPersister.rejectOrder(coreEvent)
                is UpdatedOrderEvent -> orderPersister.updateOrder(coreEvent)
                is CancelOrderEvent -> orderPersister.cancelOrder(coreEvent)
            }
            eventPersister.saveEvent(coreEvent)
        }
    }
}