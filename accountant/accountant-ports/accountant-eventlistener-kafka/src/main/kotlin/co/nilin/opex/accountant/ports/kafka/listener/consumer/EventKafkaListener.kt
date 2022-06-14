package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.spi.EventListener
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.springframework.stereotype.Component

@Component
class EventKafkaListener : EventConsumer<EventListener, String, CoreEvent>()