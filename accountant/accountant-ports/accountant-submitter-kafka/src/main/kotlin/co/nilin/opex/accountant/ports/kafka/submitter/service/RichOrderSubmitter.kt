package co.nilin.opex.accountant.ports.kafka.submitter.service

import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.spi.RichOrderPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class RichOrderSubmitter(@Qualifier("richOrderKafkaTemplate") val kafkaTemplate: KafkaTemplate<String, RichOrderEvent>) :
    RichOrderPublisher {

    private val logger = LoggerFactory.getLogger(RichOrderSubmitter::class.java)

    override suspend fun publish(order: RichOrderEvent): Unit = suspendCoroutine { cont ->
        logger.info("Submitting RichOrder")

        val sendFuture = kafkaTemplate.send("richOrder", order)
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("Error submitting RichOrder", it)
            cont.resume(Unit)
        })
    }
}