package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.WithdrawRequestEventSubmitter
import co.nilin.opex.wallet.ports.kafka.listener.model.WithdrawRequestEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.withdraw.event.enabled"], havingValue = "true", matchIfMissing = true)
class WithdrawRequestSubmitter(
    @Qualifier("withdrawRequestKafkaTemplate") private val template: KafkaTemplate<String?, WithdrawRequestEvent>
) :
    WithdrawRequestEventSubmitter {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    override suspend fun send(
        uuid: String,
        withdrawId: Long?,
        withdrawUuid: String?,
        lifecycleEventId: String?,
        walletTransactionRef: String?,
        businessReference: String?,
        currency: String,
        amount: BigDecimal,
        principalAmount: BigDecimal?,
        appliedCustomerFee: BigDecimal?,
        feeAsset: String?,
        transferCategory: String?,
        withdrawStatus: WithdrawStatus,
        createDate: LocalDateTime,
        lifecycleTransitionDate: LocalDateTime?,
        completionDate: LocalDateTime?,
        counterpartyReferenceId: String?
    ) {
        retryTemplate.execute<Unit, Exception> {
            try {
                val event = WithdrawRequestEvent(
                    uuid = uuid,
                    withdrawId = withdrawId,
                    withdrawUuid = withdrawUuid,
                    lifecycleEventId = lifecycleEventId,
                    walletTransactionRef = walletTransactionRef,
                    businessReference = businessReference,
                    currency = currency,
                    amount = amount,
                    principalAmount = principalAmount,
                    appliedCustomerFee = appliedCustomerFee,
                    feeAsset = feeAsset,
                    transferCategory = transferCategory,
                    status = withdrawStatus,
                    createDate = createDate,
                    lifecycleTransitionDate = lifecycleTransitionDate,
                    completionDate = completionDate,
                    counterpartyReferenceId = counterpartyReferenceId
                )
                val messageKey = lifecycleEventId ?: "withdraw:${withdrawId ?: uuid}:${withdrawStatus.name}"
                template.send(
                    "withdraw_request",
                    messageKey,
                    event
                ).get()
                logger.info("Withdraw request event sent")
            } catch (ex: Exception) {
                logger.error("Error sending withdraw request event", ex)
                throw ex
            }
        }
    }
}