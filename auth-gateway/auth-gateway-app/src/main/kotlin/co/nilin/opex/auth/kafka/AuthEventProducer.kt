package co.nilin.opex.auth.kafka

import co.nilin.opex.auth.config.KafkaTopics
import co.nilin.opex.auth.data.AuthEvent
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.future.asDeferred
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
class AuthEventProducer(private val template: KafkaTemplate<String, AuthEvent>) {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    fun send(event: AuthEvent) {
        retryTemplate.execute<Unit, Exception> {
            template.send(KafkaTopics.AUTH, event).whenComplete { res, error ->
                if (error != null) {
                    logger.error("Error sending auth event", error)
                    throw error
                }
                logger.info("Auth event sent")
            }
        }
    }
    fun send(event: AuthEvent) {
        retryTemplate.execute<Unit, Exception> {
            template.send(KafkaTopics.AUTH, event).whenComplete { res, error ->
                if (error != null) {
                    logger.error("Error sending auth event", error)
                    throw error
                }
                logger.info("Auth event sent")
            }
        }
    }
}