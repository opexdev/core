package co.nilin.opex.matching.engine.ports.kafka.submitter.service

import co.nilin.opex.matching.engine.core.inout.OrderBookUpdateEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class OrderBookUpdateSubmitter(private val kafkaTemplate: KafkaTemplate<String?, OrderBookUpdateEvent>) {

    private val logger = LoggerFactory.getLogger(OrderBookUpdateSubmitter::class.java)
    private var lastSent = 0L

    suspend fun submit(event: OrderBookUpdateEvent): Unit = suspendCoroutine { cont ->
        val now = System.currentTimeMillis()
        if (now - lastSent < 1000) // send every second not more
            return@suspendCoroutine

        kafkaTemplate.send("orderBookUpdate", event).addCallback(
            {
                logger.info("Orderbook update event sent")
                lastSent = System.currentTimeMillis()
                cont.resume(Unit)
            },
            {
                logger.warn("Orderbook update event failed", it)
                cont.resumeWithException(it)
            }
        )
    }
}