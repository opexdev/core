package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.funding.*
import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.DepositListener
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class EventlogDepositListener(private val fundingLog: FundingLogWriter) : DepositListener {
    override fun id(): String = "DepositListener"
    override fun onEvent(event: DepositEvent, partition: Int, offset: Long, timestamp: Long) {
        val operationId = event.depositUuid ?: event.depositRef ?: "deposit:$partition:$offset"
        val status = event.status?.uppercase() ?: "UNKNOWN"
        val eventId = event.lifecycleEventId ?: "deposit-lifecycle:$operationId:$status"
        require(operationId.isNotBlank() && eventId.isNotBlank() && event.uuid.isNotBlank()) { "Funding event identity is required" }
        val fallback = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
        val occurredAt = event.lifecycleTransitionDate ?: event.completionDate ?: event.createDate ?: fallback
        runBlocking {
            fundingLog.save(FundingRecord(
                eventId = eventId,
                operationId = operationId,
                type = "DEPOSIT",
                userId = event.uuid,
                operationDatabaseId = event.depositId,
                currency = event.currency,
                amount = event.amount,
                principalAmount = event.principalAmount,
                appliedCustomerFee = event.appliedCustomerFee,
                feeAsset = event.feeAsset,
                status = status,
                transferCategory = event.transferCategory,
                businessReference = event.businessReference,
                counterpartyReferenceId = event.depositRef,
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
                acceptedFee = event.acceptedFee,
                sourceSymbol = event.sourceSymbol,
                sourceAddress = event.sourceAddress
            ), "deposit", partition, offset)
        }
    }
}
