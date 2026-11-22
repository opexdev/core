package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.swap.*
import co.nilin.opex.eventlog.ports.postgres.dao.SwapRepository
import co.nilin.opex.eventlog.ports.postgres.model.SwapModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class SwapLogPersister(private val repository: SwapRepository) : SwapLogWriter, SwapLogReader {
    override suspend fun save(swap: SwapRecord, topic: String, partition: Int, offset: Long): SwapRecord {
        val normalized = swap.canonical()
        val model = SwapModel(
            uuid = normalized.uuid,
            txId = normalized.txId,
            sourceAsset = normalized.sourceAsset,
            sourceAmount = normalized.sourceAmount,
            destAsset = normalized.destAsset,
            destAmount = normalized.destAmount,
            sourceTransactionId = normalized.sourceTransactionId,
            destinationTransactionId = normalized.destinationTransactionId,
            sourceReference = normalized.sourceReference,
            destinationReference = normalized.destinationReference,
            systemPartyId = normalized.systemPartyId,
            receiverPartyId = normalized.receiverPartyId,
            sourceSenderWalletId = normalized.sourceSenderWalletId,
            sourceReceiverWalletId = normalized.sourceReceiverWalletId,
            destinationSenderWalletId = normalized.destinationSenderWalletId,
            destinationReceiverWalletId = normalized.destinationReceiverWalletId,
            sourceSenderWalletType = normalized.sourceSenderWalletType,
            sourceReceiverWalletType = normalized.sourceReceiverWalletType,
            destinationSenderWalletType = normalized.destinationSenderWalletType,
            destinationReceiverWalletType = normalized.destinationReceiverWalletType,
            reservationId = normalized.reservationId,
            reservationReference = normalized.reservationReference,
            quotedRate = normalized.quotedRate,
            quotedAt = normalized.quotedAt,
            quoteExpiresAt = normalized.quoteExpiresAt,
            transferCategory = normalized.transferCategory,
            reservedBy = normalized.reservedBy,
            executedBy = normalized.executedBy,
            description = normalized.description,
            status = normalized.status,
            createDate = normalized.createDate,
            sourceTopic = topic, sourcePartition = partition, sourceOffset = offset,
            capturedAt = LocalDateTime.now()
        )
        return try {
            repository.save(model).awaitFirst().toCore()
        } catch (ex: DuplicateKeyException) {
            val existing = repository.findByTxId(normalized.txId).awaitFirstOrNull()
                ?: repository.findBySourceTopicAndSourcePartitionAndSourceOffset(topic, partition, offset).awaitFirstOrNull()
                ?: throw ex
            check(existing.toCore().canonical() == normalized) { "Conflicting swap operation: " + normalized.txId }
            existing.toCore()
        }
    }

    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<SwapRecord> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        return repository.findByCreateDateBetweenOrderByCreateDateDesc(from, to, PageRequest.of(0, limit.coerceIn(1, 10_000)))
            .map { it.toCore() }.collectList().awaitFirst()
    }

    private fun SwapModel.toCore() = SwapRecord(
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
        reservationId = reservationId,
        reservationReference = reservationReference,
        quotedRate = quotedRate,
        quotedAt = quotedAt,
        quoteExpiresAt = quoteExpiresAt,
        transferCategory = transferCategory,
        reservedBy = reservedBy,
        executedBy = executedBy,
        description = description,
        status = status,
        createDate = createDate
    )

    private fun SwapRecord.canonical() = copy(
        sourceAmount = sourceAmount.stripTrailingZeros(),
        destAmount = destAmount.stripTrailingZeros(),
        quotedRate = quotedRate?.stripTrailingZeros(),
        quotedAt = quotedAt?.truncatedTo(ChronoUnit.MICROS),
        quoteExpiresAt = quoteExpiresAt?.truncatedTo(ChronoUnit.MICROS),
        createDate = createDate.truncatedTo(ChronoUnit.MICROS)
    )
}
