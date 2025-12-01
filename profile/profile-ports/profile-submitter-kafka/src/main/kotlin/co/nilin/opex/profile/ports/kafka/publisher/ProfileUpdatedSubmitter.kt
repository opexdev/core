package co.nilin.opex.profile.ports.kafka.publisher


import co.nilin.opex.profile.core.data.event.ProfileUpdatedEvent
import co.nilin.opex.profile.core.spi.ProfileUpdatedPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class ProfileUpdatedSubmitter(
    @Qualifier("profileUpdatedKafkaTemplate") private val kafkaTemplate: KafkaTemplate<String, ProfileUpdatedEvent>,
) : ProfileUpdatedPublisher {

    private val logger = LoggerFactory.getLogger(ProfileUpdatedSubmitter::class.java)

    val topic = "profile_updated"

    override suspend fun publish(event: ProfileUpdatedEvent): Unit = suspendCoroutine { cont ->
        logger.info("Submitting ProfileUpdatedEvent")

        val sendFuture = kafkaTemplate.send(topic, event)
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("Error submitting ProfileUpdatedEvent", it)
            cont.resumeWithException(it)
        })
    }


}