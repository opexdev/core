package co.nilin.opex.wallet.ports.kafka.producer.producer

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.WithdrawRequestEventProducer
import co.nilin.opex.wallet.ports.kafka.producer.config.KafkaTopics
import co.nilin.opex.wallet.ports.kafka.producer.events.WithdrawRequestEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class WithdrawRequestProducer(private val template: KafkaTemplate<String, WithdrawRequestEvent>) :
    WithdrawRequestEventProducer {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    override suspend fun send(
        uuid: String,
        withdrawId: Long?,
        currency: String,
        amount: BigDecimal,
        withdrawStatus: WithdrawStatus,
        createDate: LocalDateTime
    ) {
        retryTemplate.execute<Unit, Exception> {
            template.send(
                KafkaTopics.WITHDRAW_REQUEST,
                WithdrawRequestEvent(uuid, withdrawId, currency, amount, withdrawStatus, createDate)
            ).addCallback(
                { logger.info("Withdraw request event sent") },
                { error -> logger.error("Error sending withdraw request event", error) }
            )
        }
    }
}