package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.ports.postgres.dao.AccountingTransactionRepository
import co.nilin.opex.eventlog.ports.postgres.model.AccountingTransactionModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class AccountingTransactionLogPersister(
    private val repository: AccountingTransactionRepository
) : AccountingTransactionLogWriter, AccountingTransactionLogReader {

    override suspend fun save(transaction: AccountingTransactionEvent): AccountingTransactionEvent {
        val normalized = transaction.withResolvedEventId()
        return try {
            repository.save(normalized.toModel()).awaitFirst().toCore()
        } catch (ex: DuplicateKeyException) {
            val existing = repository.findByEventIdAndSource(normalized.eventId!!, normalized.source).awaitFirstOrNull()
                ?: repository.findByTxIdAndSource(normalized.txId, normalized.source).awaitFirstOrNull()
                ?: throw ex

            val persisted = existing.toCore().withResolvedEventId()
            if (persisted.equivalentTo(normalized)) {
                persisted
            } else {
                throw IllegalStateException(
                    "Conflicting accounting transaction payload for event identity=${normalized.eventId}, source=${normalized.source}"
                )
            }
        }
    }

    override suspend fun saveAll(transactions: List<AccountingTransactionEvent>): List<AccountingTransactionEvent> {
        return transactions.map { save(it) }
    }

    override suspend fun findBetween(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        val page = PageRequest.of(0, limit.coerceIn(1, 10_000))
        return repository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to, page)
            .map { it.toCore() }
            .collectList()
            .awaitFirst()
    }

    override suspend fun findReportableWithdrawals(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> = repository.findReportableWithdrawals(from, to, limit.coerceIn(1, 10_000))
        .map { it.toCore().let { tx -> tx.copy(occurredAt = tx.lifecycleTransitionAt ?: tx.completedAt ?: tx.occurredAt) } }
        .collectList().awaitFirst()

    private fun AccountingTransactionEvent.toModel(): AccountingTransactionModel {
        val normalized = withResolvedEventId()
        return AccountingTransactionModel(
            id = null,
            txId = normalized.txId,
            eventId = normalized.eventId,
            userId = normalized.userId,
            type = normalized.type.name,
            symbol = normalized.symbol,
            asset = normalized.asset,
            amount = normalized.amount,
            fee = normalized.fee,
            feeAsset = normalized.feeAsset,
            principalAmount = normalized.principalAmount,
            grossAmount = normalized.grossAmount,
            withdrawStatus = normalized.withdrawStatus,
            transferCategory = normalized.transferCategory,
            operationId = normalized.operationId,
            businessReference = normalized.businessReference,
            walletTransactionRef = normalized.walletTransactionRef,
            recordRole = normalized.recordRole,
            senderPartyId = normalized.senderPartyId,
            receiverPartyId = normalized.receiverPartyId,
            senderWalletId = normalized.senderWalletId,
            receiverWalletId = normalized.receiverWalletId,
            senderWalletType = normalized.senderWalletType,
            receiverWalletType = normalized.receiverWalletType,
            perspectivePartyId = normalized.perspectivePartyId,
            counterpartyReferenceId = normalized.counterpartyReferenceId,
            relatedTransactionId = normalized.relatedTransactionId,
            sourceAsset = normalized.sourceAsset,
            sourceAmount = normalized.sourceAmount,
            destinationAsset = normalized.destinationAsset,
            destinationAmount = normalized.destinationAmount,
            originalTradeId = normalized.originalTradeId,
            baseAsset = normalized.baseAsset,
            quoteAsset = normalized.quoteAsset,
            quoteAmount = normalized.quoteAmount,
            tradeSide = normalized.tradeSide,
            participantRole = normalized.participantRole,
            orderReference = normalized.orderReference,
            counterpartyOrderReference = normalized.counterpartyOrderReference,
            financialActionId = normalized.financialActionId,
            relatedFinancialActionId = normalized.relatedFinancialActionId,
            feeActionId = normalized.feeActionId,
            expectedFee = normalized.expectedFee,
            expectedFeeAsset = normalized.expectedFeeAsset,
            source = normalized.source,
            referenceId = normalized.referenceId,
            side = normalized.side,
            price = normalized.price,
            quantity = normalized.quantity,
            operationCreatedAt = normalized.operationCreatedAt,
            lifecycleTransitionAt = normalized.lifecycleTransitionAt,
            completedAt = normalized.completedAt,
            occurredAt = normalized.occurredAt,
            createDate = LocalDateTime.now()
        )
    }

    private fun AccountingTransactionModel.toCore(): AccountingTransactionEvent {
        return AccountingTransactionEvent(
            txId = txId,
            eventId = eventId ?: txId,
            userId = userId,
            type = AccountingTransactionType.valueOf(type),
            symbol = symbol,
            asset = asset,
            amount = amount,
            fee = fee,
            feeAsset = feeAsset,
            principalAmount = principalAmount,
            grossAmount = grossAmount,
            withdrawStatus = withdrawStatus,
            transferCategory = transferCategory,
            operationId = operationId,
            businessReference = businessReference,
            walletTransactionRef = walletTransactionRef,
            recordRole = recordRole,
            senderPartyId = senderPartyId,
            receiverPartyId = receiverPartyId,
            senderWalletId = senderWalletId,
            receiverWalletId = receiverWalletId,
            senderWalletType = senderWalletType,
            receiverWalletType = receiverWalletType,
            perspectivePartyId = perspectivePartyId,
            counterpartyReferenceId = counterpartyReferenceId,
            relatedTransactionId = relatedTransactionId,
            sourceAsset = sourceAsset,
            sourceAmount = sourceAmount,
            destinationAsset = destinationAsset,
            destinationAmount = destinationAmount,
            originalTradeId = originalTradeId,
            baseAsset = baseAsset,
            quoteAsset = quoteAsset,
            quoteAmount = quoteAmount,
            tradeSide = tradeSide,
            participantRole = participantRole,
            orderReference = orderReference,
            counterpartyOrderReference = counterpartyOrderReference,
            financialActionId = financialActionId,
            relatedFinancialActionId = relatedFinancialActionId,
            feeActionId = feeActionId,
            expectedFee = expectedFee,
            expectedFeeAsset = expectedFeeAsset,
            source = source,
            referenceId = referenceId,
            side = side,
            price = price,
            quantity = quantity,
            operationCreatedAt = operationCreatedAt,
            lifecycleTransitionAt = lifecycleTransitionAt,
            completedAt = completedAt,
            occurredAt = occurredAt
        )
    }

    private fun AccountingTransactionEvent.withResolvedEventId(): AccountingTransactionEvent {
        return if (eventId.isNullOrBlank()) copy(eventId = txId) else this
    }

    private fun AccountingTransactionEvent.equivalentTo(other: AccountingTransactionEvent): Boolean {
        return txId == other.txId &&
            eventId == other.eventId &&
            userId == other.userId &&
            type == other.type &&
            symbol == other.symbol &&
            asset == other.asset &&
            amount.numericallyEquals(other.amount) &&
            fee.numericallyEquals(other.fee) &&
            feeAsset == other.feeAsset &&
            principalAmount.numericallyEquals(other.principalAmount) &&
            grossAmount.numericallyEquals(other.grossAmount) &&
            withdrawStatus == other.withdrawStatus &&
            transferCategory == other.transferCategory &&
            operationId == other.operationId &&
            businessReference == other.businessReference &&
            walletTransactionRef == other.walletTransactionRef &&
            recordRole == other.recordRole &&
            senderPartyId == other.senderPartyId &&
            receiverPartyId == other.receiverPartyId &&
            senderWalletId == other.senderWalletId &&
            receiverWalletId == other.receiverWalletId &&
            senderWalletType == other.senderWalletType &&
            receiverWalletType == other.receiverWalletType &&
            perspectivePartyId == other.perspectivePartyId &&
            counterpartyReferenceId == other.counterpartyReferenceId &&
            relatedTransactionId == other.relatedTransactionId &&
            sourceAsset == other.sourceAsset &&
            sourceAmount.numericallyEquals(other.sourceAmount) &&
            destinationAsset == other.destinationAsset &&
            destinationAmount.numericallyEquals(other.destinationAmount) &&
            originalTradeId == other.originalTradeId &&
            baseAsset == other.baseAsset &&
            quoteAsset == other.quoteAsset &&
            quoteAmount.numericallyEquals(other.quoteAmount) &&
            tradeSide == other.tradeSide &&
            participantRole == other.participantRole &&
            orderReference == other.orderReference &&
            counterpartyOrderReference == other.counterpartyOrderReference &&
            financialActionId == other.financialActionId &&
            relatedFinancialActionId == other.relatedFinancialActionId &&
            feeActionId == other.feeActionId &&
            expectedFee.numericallyEquals(other.expectedFee) &&
            expectedFeeAsset == other.expectedFeeAsset &&
            source == other.source &&
            referenceId == other.referenceId &&
            side == other.side &&
            price.numericallyEquals(other.price) &&
            quantity.numericallyEquals(other.quantity) &&
            operationCreatedAt.sameDatabasePrecision(other.operationCreatedAt) &&
            lifecycleTransitionAt.sameDatabasePrecision(other.lifecycleTransitionAt) &&
            completedAt.sameDatabasePrecision(other.completedAt) &&
            occurredAt.sameDatabasePrecision(other.occurredAt)
    }

    private fun BigDecimal?.numericallyEquals(other: BigDecimal?): Boolean {
        if (this == null || other == null) return this == other
        return this.compareTo(other) == 0
    }

    private fun LocalDateTime?.sameDatabasePrecision(other: LocalDateTime?): Boolean {
        if (this == null || other == null) return this == other
        return this.truncatedTo(ChronoUnit.MICROS) == other.truncatedTo(ChronoUnit.MICROS)
    }
}
