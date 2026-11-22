package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.trade.*
import co.nilin.opex.eventlog.ports.postgres.dao.RichTradeRepository
import co.nilin.opex.eventlog.ports.postgres.model.RichTradeModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class RichTradeLogPersister(private val repository: RichTradeRepository) : RichTradeLogWriter, RichTradeLogReader {
    override suspend fun save(trade: RichTradeRecord, topic: String, partition: Int, offset: Long): RichTradeRecord {
        val normalized = trade.canonical()
        val model = RichTradeModel(
            executionReference = normalized.executionReference,
            originalTradeId = normalized.originalTradeId,
            pair = normalized.pair,
            takerOuid = normalized.takerOuid,
            takerUuid = normalized.takerUuid,
            takerOrderId = normalized.takerOrderId,
            takerDirection = normalized.takerDirection,
            takerPrice = normalized.takerPrice,
            takerQuantity = normalized.takerQuantity,
            takerQuoteQuantity = normalized.takerQuoteQuantity,
            takerRemainedQuantity = normalized.takerRemainedQuantity,
            takerCommision = normalized.takerCommision,
            takerCommisionAsset = normalized.takerCommisionAsset,
            makerOuid = normalized.makerOuid,
            makerUuid = normalized.makerUuid,
            makerOrderId = normalized.makerOrderId,
            makerDirection = normalized.makerDirection,
            makerPrice = normalized.makerPrice,
            makerQuantity = normalized.makerQuantity,
            makerQuoteQuantity = normalized.makerQuoteQuantity,
            makerRemainedQuantity = normalized.makerRemainedQuantity,
            makerCommision = normalized.makerCommision,
            makerCommisionAsset = normalized.makerCommisionAsset,
            matchedPrice = normalized.matchedPrice,
            matchedQuantity = normalized.matchedQuantity,
            tradeDateTime = normalized.tradeDateTime,
            baseAsset = normalized.baseAsset,
            quoteAsset = normalized.quoteAsset,
            takerSettlementActionId = normalized.takerSettlementActionId,
            makerSettlementActionId = normalized.makerSettlementActionId,
            takerFeeActionId = normalized.takerFeeActionId,
            makerFeeActionId = normalized.makerFeeActionId,
            sourceTopic = topic, sourcePartition = partition, sourceOffset = offset,
            capturedAt = LocalDateTime.now()
        )
        return try {
            repository.save(model).awaitFirst().toCore()
        } catch (ex: DuplicateKeyException) {
            val existing = repository.findByExecutionReference(normalized.executionReference).awaitFirstOrNull()
                ?: repository.findBySourceTopicAndSourcePartitionAndSourceOffset(topic, partition, offset).awaitFirstOrNull()
                ?: throw ex
            check(existing.toCore().canonical() == normalized) { "Conflicting richTrade execution: " + normalized.executionReference }
            existing.toCore()
        }
    }

    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<RichTradeRecord> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        return repository.findByTradeDateTimeBetweenOrderByTradeDateTimeDesc(from, to, PageRequest.of(0, limit.coerceIn(1, 10_000)))
            .map { it.toCore() }.collectList().awaitFirst()
    }

    private fun RichTradeModel.toCore() = RichTradeRecord(
        executionReference = executionReference,
        originalTradeId = originalTradeId,
        pair = pair,
        takerOuid = takerOuid,
        takerUuid = takerUuid,
        takerOrderId = takerOrderId,
        takerDirection = takerDirection,
        takerPrice = takerPrice,
        takerQuantity = takerQuantity,
        takerQuoteQuantity = takerQuoteQuantity,
        takerRemainedQuantity = takerRemainedQuantity,
        takerCommision = takerCommision,
        takerCommisionAsset = takerCommisionAsset,
        makerOuid = makerOuid,
        makerUuid = makerUuid,
        makerOrderId = makerOrderId,
        makerDirection = makerDirection,
        makerPrice = makerPrice,
        makerQuantity = makerQuantity,
        makerQuoteQuantity = makerQuoteQuantity,
        makerRemainedQuantity = makerRemainedQuantity,
        makerCommision = makerCommision,
        makerCommisionAsset = makerCommisionAsset,
        matchedPrice = matchedPrice,
        matchedQuantity = matchedQuantity,
        tradeDateTime = tradeDateTime,
        baseAsset = baseAsset,
        quoteAsset = quoteAsset,
        takerSettlementActionId = takerSettlementActionId,
        makerSettlementActionId = makerSettlementActionId,
        takerFeeActionId = takerFeeActionId,
        makerFeeActionId = makerFeeActionId
    )

    private fun RichTradeRecord.canonical() = copy(
        takerPrice = takerPrice.stripTrailingZeros(),
        takerQuantity = takerQuantity.stripTrailingZeros(),
        takerQuoteQuantity = takerQuoteQuantity.stripTrailingZeros(),
        takerRemainedQuantity = takerRemainedQuantity.stripTrailingZeros(),
        takerCommision = takerCommision.stripTrailingZeros(),
        makerPrice = makerPrice.stripTrailingZeros(),
        makerQuantity = makerQuantity.stripTrailingZeros(),
        makerQuoteQuantity = makerQuoteQuantity.stripTrailingZeros(),
        makerRemainedQuantity = makerRemainedQuantity.stripTrailingZeros(),
        makerCommision = makerCommision.stripTrailingZeros(),
        matchedPrice = matchedPrice.stripTrailingZeros(),
        matchedQuantity = matchedQuantity.stripTrailingZeros(),
        tradeDateTime = tradeDateTime.truncatedTo(ChronoUnit.MICROS)
    )
}
