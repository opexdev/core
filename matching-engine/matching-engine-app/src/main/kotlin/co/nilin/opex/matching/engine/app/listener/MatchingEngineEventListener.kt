package co.nilin.opex.matching.engine.app.listener

import co.nilin.opex.matching.engine.app.bl.OrderBooks
import co.nilin.opex.matching.engine.core.eventh.events.CancelOrderEvent
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.core.eventh.events.EditOrderRequestEvent
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderEditCommand
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.EventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class MatchingEngineEventListener : EventListener {

    private val logger = LoggerFactory.getLogger(MatchingEngineEventListener::class.java)

    override fun id(): String {
        return "EventListener"
    }

    override fun onEvent(event: CoreEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("Received CoreEvent: ${event::class.java}")

        runBlocking {
            val orderBook = OrderBooks.lookupOrderBook("${event.pair.leftSideName}_${event.pair.rightSideName}")

            when (event) {
                is EditOrderRequestEvent -> orderBook.handleEditCommand(
                    OrderEditCommand(
                        event.ouid,
                        event.uuid,
                        event.orderId,
                        event.pair,
                        event.price,
                        event.quantity
                    )
                )

                is CancelOrderEvent -> orderBook.handleCancelCommand(
                    OrderCancelCommand(
                        event.ouid,
                        event.uuid,
                        event.orderId,
                        event.pair
                    )
                )
                else -> null
            }
        }
    }
}