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
        depositUuid: String?,
        lifecycleEventId: String?,
        walletTransactionRef: String?,
        businessReference: String?,
        currency: String,
        amount: BigDecimal,
        principalAmount: BigDecimal?,
        appliedCustomerFee: BigDecimal?,
        feeAsset: String?,
        transferCategory: String?,
        status: String?,
        createDate: LocalDateTime?,
        lifecycleTransitionDate: LocalDateTime?,
        completionDate: LocalDateTime?
    ) {
        retryTemplate.execute<Unit, Exception> {
            try {
                val event = DepositEvent(
                    uuid = uuid,
                    depositRef = depositRef,
                    depositUuid = depositUuid,
                    lifecycleEventId = lifecycleEventId,
                    walletTransactionRef = walletTransactionRef,
                    businessReference = businessReference,
                    currency = currency,
                    amount = amount,
                    principalAmount = principalAmount,
                    appliedCustomerFee = appliedCustomerFee,
                    feeAsset = feeAsset,
                    transferCategory = transferCategory,
                    status = status,
                    createDate = createDate,
                    lifecycleTransitionDate = lifecycleTransitionDate,
                    completionDate = completionDate
                )
                val messageKey = lifecycleEventId ?: "deposit:${depositUuid ?: depositRef ?: uuid}:${status ?: "UNKNOWN"}"
                template.send(
                    "deposit",
                    messageKey,
                    event
                ).get()
                logger.info("Deposit event sent")
            } catch (ex: Exception) {
                logger.error("Error sending deposit event", ex)
                throw ex
            }
        }
    }
}