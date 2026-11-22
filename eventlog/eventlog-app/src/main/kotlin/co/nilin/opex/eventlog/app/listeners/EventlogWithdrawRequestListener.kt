package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.funding.*
import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.WithdrawRequestListener
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class EventlogWithdrawRequestListener(private val fundingLog: FundingLogWriter) : WithdrawRequestListener {
    override fun id(): String = "WithdrawRequestListener"
    override fun onEvent(event: WithdrawRequestEvent, partition: Int, offset: Long, timestamp: Long) {
        val operationId = event.withdrawUuid ?: event.withdrawId?.toString() ?: "withdraw_request:$partition:$offset"
        val status = event.status.uppercase()
        val eventId = event.lifecycleEventId ?: "withdraw-lifecycle:$operationId:$status"
        require(operationId.isNotBlank() && eventId.isNotBlank() && event.uuid.isNotBlank()) { "Funding event identity is required" }
        val fallback = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
        val occurredAt = event.lifecycleTransitionDate ?: event.completionDate ?: event.createDate
        runBlocking {
            fundingLog.save(FundingRecord(
                eventId = eventId,
                operationId = operationId,
                type = "WITHDRAW",
                userId = event.uuid,
                operationDatabaseId = event.withdrawId,
                currency = event.currency,
                amount = event.amount,
                principalAmount = event.principalAmount,
                appliedCustomerFee = event.appliedCustomerFee,
                feeAsset = event.feeAsset,
                status = status,
                transferCategory = event.transferCategory,
                businessReference = event.businessReference,
                counterpartyReferenceId = event.counterpartyReferenceId,
                walletTransactionRef = event.walletTransactionRef,
                operationCreatedAt = event.createDate,
                lifecycleTransitionAt = event.lifecycleTransitionDate,
                completedAt = event.completionDate,
                occurredAt = occurredAt,
                transferMethod = event.transferMethod,
                operationType = event.operationType,
                applicator = event.applicator,
                gatewayId = event.gatewayId,
                network = event.network,
                note = event.note,
                attachment = event.attachment,
                senderPartyId = event.senderPartyId,
                receiverPartyId = event.receiverPartyId,
                senderWalletId = event.senderWalletId,
                receiverWalletId = event.receiverWalletId,
                senderWalletType = event.senderWalletType,
                receiverWalletType = event.receiverWalletType,
                destinationAddress = event.destinationAddress,
                destinationAsset = event.destinationAsset,
                destinationAmount = event.destinationAmount,
                requestTransactionId = event.requestTransactionId,
                finalizedTransactionId = event.finalizedTransactionId,
                operationWalletId = event.operationWalletId,
                otpRequired = event.otpRequired,
                statusReason = event.statusReason
            ), "withdraw_request", partition, offset)
        }
    }
}
