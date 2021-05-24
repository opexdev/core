package co.nilin.mixchange.app.bl

import co.nilin.mixchange.matching.core.eventh.EventDispatcher
import co.nilin.mixchange.matching.core.eventh.events.*
import co.nilin.mixchange.port.order.kafka.service.EventsSubmitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class ExchangeEventHandler(eventsSubmitter: EventsSubmitter)
{
    fun register(){
        EventDispatcher.register(CreateOrderEvent::class.java, handler)
        EventDispatcher.register(CancelOrderEvent::class.java, handler)
        EventDispatcher.register(UpdatedOrderEvent::class.java, handler)
        EventDispatcher.register(RejectOrderEvent::class.java, handler)
        EventDispatcher.register(SubmitOrderEvent::class.java, handler)
        EventDispatcher.register(TradeEvent::class.java, handler)
    }

    val handler: (CoreEvent) -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            eventsSubmitter.submit(it)
        }
    }

}