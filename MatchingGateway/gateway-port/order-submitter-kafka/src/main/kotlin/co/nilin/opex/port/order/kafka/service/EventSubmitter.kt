package co.nilin.opex.port.order.kafka.service

import co.nilin.opex.matching.core.eventh.events.CoreEvent
import co.nilin.opex.port.order.kafka.inout.OrderSubmitResult
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class EventSubmitter(val kafkaTemplate: KafkaTemplate<String, CoreEvent>) {

    private val logger = LoggerFactory.getLogger(EventSubmitter::class.java)

    suspend fun submit(event: CoreEvent): OrderSubmitResult = suspendCoroutine {
        logger.info("Submit event for pair ${event.pair} = ${event::class.java}")
        val sendFuture = kafkaTemplate.send("events_${event.pair.leftSideName}_${event.pair.rightSideName}", event)

        sendFuture.addCallback({ sendResult ->
            it.resume(OrderSubmitResult(sendResult?.recordMetadata?.offset()))
        }, { exception ->
            it.resumeWithException(exception)
        })
    }

}