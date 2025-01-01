package co.nilin.opex.matching.gateway.ports.kafka.submitter.service

import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderRequestEvent
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitResult
import co.nilin.opex.matching.gateway.ports.kafka.submitter.utils.EventSubmitterInfo
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class OrderRequestEventSubmitter(
    private val kafkaTemplate: KafkaTemplate<String, OrderRequestEvent>,
    private val eventSubmitterInfo: EventSubmitterInfo
) {

    private val logger = LoggerFactory.getLogger(OrderRequestEventSubmitter::class.java)

    suspend fun submit(order: OrderRequestEvent): OrderSubmitResult = suspendCoroutine { cont ->
        logger.info("Submitting OrderRequestEvent: ouid=${order.ouid}")

        val sendFuture = kafkaTemplate.send("orders_${order.pair.leftSideName}_${order.pair.rightSideName}", order)
        sendFuture.addCallback({
            cont.resume(OrderSubmitResult(it?.recordMetadata?.offset()))
            eventSubmitterInfo.updateLastProcessedOrderRequestTime()
        }, {
            logger.error("Error submitting OrderSubmitRequest", it)
            cont.resumeWithException(it)
        })
    }

}