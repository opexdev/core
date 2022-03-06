package co.nilin.opex.admin.ports.kafka.submitter.service

import co.nilin.opex.admin.core.events.AdminEvent
import co.nilin.opex.admin.core.spi.AdminEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class AdminKafkaEventPublisher(private val kafkaTemplate: KafkaTemplate<String?, AdminEvent>) : AdminEventPublisher {

    private val logger = LoggerFactory.getLogger(AdminKafkaEventPublisher::class.java)

    override suspend fun publish(event: AdminEvent): Unit = suspendCoroutine { cont ->
        logger.info("Publishing admin event: $event")

        val sendFuture = kafkaTemplate.send("admin_event", event)
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("Error publishing admin event", it)
            cont.resumeWithException(it)
        })
    }

}