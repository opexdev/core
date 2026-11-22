package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.funding.*
import co.nilin.opex.eventlog.ports.postgres.dao.FundingRepository
import co.nilin.opex.eventlog.ports.postgres.model.FundingModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class FundingLogPersister(private val repository: FundingRepository) : FundingLogWriter, FundingLogReader {
    override suspend fun findOperation(type: String, operationId: String): List<FundingRecord> {
        require(type in setOf("DEPOSIT", "WITHDRAW")) { "Unsupported funding type" }
        require(operationId.isNotBlank()) { "Operation ID is required" }
        return repository.findByTypeAndOperationIdOrderByOccurredAtAsc(type, operationId)
            .map { it.toCore() }.collectList().awaitFirst()
    }

    override suspend fun save(record: FundingRecord, topic: String, partition: Int, offset: Long): FundingRecord {
        val normalized = record.canonical()
        val model = FundingModel(
            eventId = normalized.eventId,
            operationId = normalized.operationId,
            type = normalized.type,
            userId = normalized.userId,
            operationDatabaseId = normalized.operationDatabaseId,
            currency = normalized.currency,
            amount = normalized.amount,
            principalAmount = normalized.principalAmount,
            appliedCustomerFee = normalized.appliedCustomerFee,
            feeAsset = normalized.feeAsset,
            acceptedFee = normalized.acceptedFee,
            status = normalized.status,
            transferCategory = normalized.transferCategory,
            transferMethod = normalized.transferMethod,
            operationType = normalized.operationType,
            applicator = normalized.applicator,
            gatewayId = normalized.gatewayId,
            network = normalized.network,
            businessReference = normalized.businessReference,
            counterpartyReferenceId = normalized.counterpartyReferenceId,
            walletTransactionRef = normalized.walletTransactionRef,
            requestTransactionId = normalized.requestTransactionId,
            finalizedTransactionId = normalized.finalizedTransactionId,
            operationWalletId = normalized.operationWalletId,
            senderPartyId = normalized.senderPartyId,
            receiverPartyId = normalized.receiverPartyId,
            senderWalletId = normalized.senderWalletId,
            receiverWalletId = normalized.receiverWalletId,
            senderWalletType = normalized.senderWalletType,
            receiverWalletType = normalized.receiverWalletType,
            sourceSymbol = normalized.sourceSymbol,
            sourceAddress = normalized.sourceAddress,
            destinationAddress = normalized.destinationAddress,
            destinationAsset = normalized.destinationAsset,
            destinationAmount = normalized.destinationAmount,
            note = normalized.note,
            statusReason = normalized.statusReason,
            attachment = normalized.attachment,
            otpRequired = normalized.otpRequired,
            operationCreatedAt = normalized.operationCreatedAt,
            lifecycleTransitionAt = normalized.lifecycleTransitionAt,
            completedAt = normalized.completedAt,
            occurredAt = normalized.occurredAt,
            sourceTopic = topic, sourcePartition = partition, sourceOffset = offset, capturedAt = LocalDateTime.now()
        )
        return try { repository.save(model).awaitFirst().toCore() }
        catch (ex: DuplicateKeyException) {
            val existing = repository.findByTypeAndEventId(normalized.type, normalized.eventId).awaitFirstOrNull()
                ?: repository.findBySourceTopicAndSourcePartitionAndSourceOffset(topic, partition, offset).awaitFirstOrNull()
                ?: throw ex
            check(existing.toCore().canonical() == normalized) { "Conflicting funding lifecycle event: " + normalized.eventId }
            existing.toCore()
        }
    }

    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<FundingRecord> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        return repository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to, PageRequest.of(0, limit.coerceIn(1, 10_000)))
            .map { it.toCore() }.collectList().awaitFirst()
    }

    override suspend fun findReportable(from: LocalDateTime, to: LocalDateTime, limit: Int): List<FundingRecord> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        return repository.findReportable(from, to, limit.coerceIn(1, 10_000)).map { it.toCore() }.collectList().awaitFirst()
    }

    private fun FundingModel.toCore() = FundingRecord(
        eventId = eventId,
        operationId = operationId,
        type = type,
        userId = userId,
        operationDatabaseId = operationDatabaseId,
        currency = currency,
        amount = amount,
        principalAmount = principalAmount,
        appliedCustomerFee = appliedCustomerFee,
        feeAsset = feeAsset,
        acceptedFee = acceptedFee,
        status = status,
        transferCategory = transferCategory,
        transferMethod = transferMethod,
        operationType = operationType,
        applicator = applicator,
        gatewayId = gatewayId,
        network = network,
        businessReference = businessReference,
        counterpartyReferenceId = counterpartyReferenceId,
        walletTransactionRef = walletTransactionRef,
        requestTransactionId = requestTransactionId,
        finalizedTransactionId = finalizedTransactionId,
        operationWalletId = operationWalletId,
        senderPartyId = senderPartyId,
        receiverPartyId = receiverPartyId,
        senderWalletId = senderWalletId,
        receiverWalletId = receiverWalletId,
        senderWalletType = senderWalletType,
        receiverWalletType = receiverWalletType,
        sourceSymbol = sourceSymbol,
        sourceAddress = sourceAddress,
        destinationAddress = destinationAddress,
        destinationAsset = destinationAsset,
        destinationAmount = destinationAmount,
        note = note,
        statusReason = statusReason,
        attachment = attachment,
        otpRequired = otpRequired,
        operationCreatedAt = operationCreatedAt,
        lifecycleTransitionAt = lifecycleTransitionAt,
        completedAt = completedAt,
        occurredAt = occurredAt
    )

    private fun FundingRecord.canonical() = copy(
        amount = amount.stripTrailingZeros(),
        principalAmount = principalAmount?.stripTrailingZeros(),
        appliedCustomerFee = appliedCustomerFee?.stripTrailingZeros(),
        acceptedFee = acceptedFee?.stripTrailingZeros(),
        destinationAmount = destinationAmount?.stripTrailingZeros(),
        operationCreatedAt = operationCreatedAt?.truncatedTo(ChronoUnit.MICROS),
        lifecycleTransitionAt = lifecycleTransitionAt?.truncatedTo(ChronoUnit.MICROS),
        completedAt = completedAt?.truncatedTo(ChronoUnit.MICROS),
        occurredAt = occurredAt.truncatedTo(ChronoUnit.MICROS)
    )
}
