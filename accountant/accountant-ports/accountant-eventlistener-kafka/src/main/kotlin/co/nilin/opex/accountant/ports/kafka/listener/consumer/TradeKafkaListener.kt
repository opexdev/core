package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.spi.TradeListener
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.springframework.stereotype.Component

@Component
class TradeKafkaListener : EventConsumer<TradeListener, String, TradeEvent>()