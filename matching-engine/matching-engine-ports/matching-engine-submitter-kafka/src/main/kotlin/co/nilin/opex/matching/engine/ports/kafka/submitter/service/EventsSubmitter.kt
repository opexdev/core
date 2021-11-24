package co.nilin.opex.matching.engine.ports.kafka.submitter.service

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.suspendCoroutine

@Component
class EventsSubmitter(val kafkaTemplate: KafkaTemplate<String, CoreEvent>) {
    suspend fun submit(event: CoreEvent): Unit = suspendCoroutine { cont ->
        println("Submit!")
        if (event is TradeEvent)
            kafkaTemplate.send("trades_${event.pair.leftSideName}_${event.pair.rightSideName}", event)
        kafkaTemplate.send("events_${event.pair.leftSideName}_${event.pair.rightSideName}", event)
    }

}