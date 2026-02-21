package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.wallet.core.spi.DepositEventSubmitter
import co.nilin.opex.wallet.ports.kafka.listener.model.DepositEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.deposit.snapshot.enabled"], havingValue = "true")
class DepositSubmitter(
    @Qualifier("depositKafkaTemplate") private val template: KafkaTemplate<String, DepositEvent>
) : DepositEventSubmitter {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    override suspend fun send(
        uuid: String,
        depositRef: String?,
        currency: String,
        amount: BigDecimal,
        createDate: LocalDateTime?
    ) {
        retryTemplate.execute<Unit, Exception> {
            try {
                template.send(
                    "deposit",
                    DepositEvent(uuid, depositRef, currency, amount, createDate)
                ).get()
                logger.info("Deposit event sent")
            } catch (ex: Exception) {
                logger.error("Error sending deposit event", ex)
                throw ex
            }
        }
    }
}