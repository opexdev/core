package co.nilin.opex.wallet.ports.kafka.listener.submitter

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.wallet.core.spi.SwapEventSubmitter
import co.nilin.opex.wallet.ports.kafka.listener.model.SwapEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.swap.event.enabled"], havingValue = "true", matchIfMissing = true)
class SwapSubmitter(
    @Qualifier("swapKafkaTemplate")
    private val template: KafkaTemplate<String, SwapEvent>
) : SwapEventSubmitter {

    private val logger by LoggerDelegate()

    private val retryTemplate = RetryTemplate.builder()
        .maxAttempts(10)
        .exponentialBackoff(1000, 1.8, 5 * 60 * 1000)
        .retryOn(Exception::class.java)
        .build()

    override suspend fun send(
        uuid: String,
        txId: String,
        sourceAsset: String,
        sourceAmount: BigDecimal,
        destAsset: String,
        destAmount: BigDecimal,
        createDate: LocalDateTime,
        sourceTransactionId: String?,
        destinationTransactionId: String?,
        sourceReference: String?,
        destinationReference: String?,
        systemPartyId: String?,
        receiverPartyId: String?,
        sourceSenderWalletId: String?,
        sourceReceiverWalletId: String?,
        destinationSenderWalletId: String?,
        destinationReceiverWalletId: String?,
        sourceSenderWalletType: String?,
        sourceReceiverWalletType: String?,
        destinationSenderWalletType: String?,
        destinationReceiverWalletType: String?,
        reservationId: Long?,
        reservationReference: String?,
        quotedRate: BigDecimal?,
        quotedAt: LocalDateTime?,
        quoteExpiresAt: LocalDateTime?,
        transferCategory: String?,
        reservedBy: String?,
        executedBy: String?,
        description: String?,
        status: String?
    ) {
        retryTemplate.execute<Unit, Exception> {
            try {
                template.send(
                    "swap",
                    uuid,
                    SwapEvent(
                        uuid = uuid,
                        txId = txId,
                        sourceAsset = sourceAsset,
                        sourceAmount = sourceAmount,
                        destAsset = destAsset,
                        destAmount = destAmount,
                        sourceTransactionId = sourceTransactionId,
                        destinationTransactionId = destinationTransactionId,
                        sourceReference = sourceReference,
                        destinationReference = destinationReference,
                        systemPartyId = systemPartyId,
                        receiverPartyId = receiverPartyId,
                        sourceSenderWalletId = sourceSenderWalletId,
                        sourceReceiverWalletId = sourceReceiverWalletId,
                        destinationSenderWalletId = destinationSenderWalletId,
                        destinationReceiverWalletId = destinationReceiverWalletId,
                        sourceSenderWalletType = sourceSenderWalletType,
                        sourceReceiverWalletType = sourceReceiverWalletType,
                        destinationSenderWalletType = destinationSenderWalletType,
                        destinationReceiverWalletType = destinationReceiverWalletType,
                        createDate = createDate,
                        reservationId = reservationId,
                        reservationReference = reservationReference,
                        quotedRate = quotedRate,
                        quotedAt = quotedAt,
                        quoteExpiresAt = quoteExpiresAt,
                        transferCategory = transferCategory,
                        reservedBy = reservedBy,
                        executedBy = executedBy,
                        description = description,
                        status = status
                    )
                ).get()
                logger.info("Swap event sent txId={} userId={}", txId, uuid)
            } catch (ex: Exception) {
                logger.error("Error sending swap event", ex)
                throw ex
            }
        }
    }
}
