package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.wallet.core.model.AccountingTransactionEvent
import co.nilin.opex.wallet.core.spi.AccountingTransactionEventSubmitter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.accounting.event.enabled"], havingValue = "true", matchIfMissing = true)
class AccountingTransactionSubmitter(
    @Qualifier("accountingTransactionKafkaTemplate")
    private val template: KafkaTemplate<String, AccountingTransactionEvent>
) : AccountingTransactionEventSubmitter {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    override suspend fun send(event: AccountingTransactionEvent) {
        retryTemplate.execute<Unit, Exception> {
            try {
                template.send("accounting_transactions", event).get()
                logger.info("Accounting transaction event sent: txId={}, type={}", event.txId, event.type)
            } catch (ex: Exception) {
                logger.error("Error sending accounting transaction event", ex)
                throw ex
            }
        }
    }
}
