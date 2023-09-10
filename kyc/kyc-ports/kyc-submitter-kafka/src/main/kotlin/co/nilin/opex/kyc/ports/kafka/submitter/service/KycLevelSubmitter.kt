package co.nilin.opex.kyc.ports.kafka.submitter.service


import co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.kyc.core.spi.KycLevelUpdatedPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class KycLevelSubmitter(
        @Qualifier("kycEventKafkaTemplate")
        private val kafkaTemplate: KafkaTemplate<String, KycLevelUpdatedEvent>,
) : KycLevelUpdatedPublisher, EventPublisher {

    private val logger = LoggerFactory.getLogger(KycLevelSubmitter::class.java)

    override val topic = "kyc_level_updated"

    override suspend fun publish(update: KycLevelUpdatedEvent): Unit = suspendCoroutine { cont ->
        logger.info("Submitting kycLevelUpdated")

        val sendFuture = kafkaTemplate.send(topic, update)
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("Error submitting kycLevelChange", it)
            cont.resumeWithException(it)
        })
    }


}