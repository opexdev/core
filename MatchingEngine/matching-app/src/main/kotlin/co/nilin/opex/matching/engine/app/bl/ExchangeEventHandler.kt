package co.nilin.opex.matching.engine.app.bl

import co.nilin.opex.matching.engine.app.config.AppSchedulers
import co.nilin.opex.matching.core.eventh.EventDispatcher
import co.nilin.opex.matching.core.eventh.events.*
import co.nilin.opex.matching.core.spi.OrderBookPersister
import co.nilin.opex.port.order.kafka.service.EventsSubmitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class ExchangeEventHandler(
    eventsSubmitter: EventsSubmitter, orderBookPersister: OrderBookPersister
) {
    fun register() {
        EventDispatcher.register(CreateOrderEvent::class.java, handler)
        EventDispatcher.register(CancelOrderEvent::class.java, handler)
        EventDispatcher.register(UpdatedOrderEvent::class.java, handler)
        EventDispatcher.register(RejectOrderEvent::class.java, handler)
        EventDispatcher.register(SubmitOrderEvent::class.java, handler)
        EventDispatcher.register(TradeEvent::class.java, handler)
        EventDispatcher.register(OrderBookPublishedEvent::class.java, localHandler)
    }

    val handler: (CoreEvent) -> Unit = {
        CoroutineScope(AppSchedulers.generalExecutor).launch {
            eventsSubmitter.submit(it)
        }
    }

    val localHandler: (OrderBookPublishedEvent) -> Unit = {
        CoroutineScope(AppSchedulers.generalExecutor).launch {
            orderBookPersister.storeLastState(it.persistentOrderBook)
        }
    }

}