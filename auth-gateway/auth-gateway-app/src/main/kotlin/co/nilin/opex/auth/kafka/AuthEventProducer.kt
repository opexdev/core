package co.nilin.opex.auth.kafka

import co.nilin.opex.auth.config.KafkaTopics
import co.nilin.opex.auth.data.AuthEvent
import co.nilin.opex.auth.data.LoginEvent
import co.nilin.opex.auth.data.LogoutEvent
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
class AuthEventProducer(private val authTemplate: KafkaTemplate<String, AuthEvent>,
                        private val loginTemplate: KafkaTemplate<String, AuthEvent>) {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    fun send(event: AuthEvent) {
        retryTemplate.execute<Unit, Exception> {
            authTemplate.send(KafkaTopics.AUTH, event).whenComplete { res, error ->
                if (error != null) {
                    logger.error("Error sending auth event", error)
                    throw error
                }
                logger.info("Auth event sent")
            }
        }
    }
    fun send(event: LoginEvent) {
        retryTemplate.execute<Unit, Exception> {
            loginTemplate.send(KafkaTopics.LOGIN, event).whenComplete { res, error ->
                if (error != null) {
                    logger.error("Error sending login event", error)
                    throw error
                }
                logger.info("login event sent")
            }
        }
    }
    fun send(event: LogoutEvent) {
        retryTemplate.execute<Unit, Exception> {
            loginTemplate.send(KafkaTopics.LOGOUT, event).whenComplete { res, error ->
                if (error != null) {
                    logger.error("Error sending logout event", error)
                    throw error
                }
                logger.info("logout event sent")
            }
        }
    }
}