package co.nilin.opex.accountant.ports.kafka.submitter.service

import co.nilin.opex.accountant.core.spi.TempEventRepublisher
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class TempEventSubmitter(
    @Qualifier("accountantEventKafkaTemplate")
    private val kafkaTemplate: KafkaTemplate<String, CoreEvent>
) : TempEventRepublisher, EventPublisher {

    private val logger = LoggerFactory.getLogger(TempEventSubmitter::class.java)

    override val topic = "tempevents"

    override suspend fun republish(events: List<CoreEvent>): Unit = suspendCoroutine { cont ->
        logger.info("Submitting TempEvents")

        events.forEach { event ->
            val sendFuture = kafkaTemplate.send(topic, event)
            sendFuture.addCallback({
                cont.resume(Unit)
            }, {
                logger.error("Error submitting TempEvents", it)
                cont.resumeWithException(it)
            })
        }
    }
}