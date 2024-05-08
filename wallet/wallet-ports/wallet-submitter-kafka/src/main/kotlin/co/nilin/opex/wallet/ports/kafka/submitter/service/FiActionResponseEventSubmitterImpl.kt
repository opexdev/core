package co.nilin.opex.wallet.ports.kafka.submitter.service

import co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent
import co.nilin.opex.wallet.core.spi.FiActionResponseEventSubmitter
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class FiActionResponseEventSubmitterImpl(private val template: KafkaTemplate<String, FinancialActionResponseEvent>) :
    FiActionResponseEventSubmitter {

    private val logger = LoggerFactory.getLogger(FiActionResponseEventSubmitterImpl::class.java)

    private val topic = "fiAction_response"

    override suspend fun submit(event: FinancialActionResponseEvent): Unit = suspendCoroutine { cont ->
        logger.info("Submitting FinancialActionResponseEvent")

        val sendFuture = template.send(topic, event)
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("Error submitting FinancialActionResponseEvent", it)
            cont.resumeWithException(it)
        })
    }
}