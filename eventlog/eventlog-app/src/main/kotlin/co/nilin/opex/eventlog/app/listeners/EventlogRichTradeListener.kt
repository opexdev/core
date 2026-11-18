package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.*
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

/** Executed quantities are matchedQuantity/matchedPrice, never original order quantities/prices. */
data class RichTradeAccountingEvent(
    val id: Long,
    val pair: String,
    val takerOuid: String,
    val takerUuid: String,
    val takerDirection: String,
    val makerOuid: String,
    val makerUuid: String,
    val makerDirection: String,
    val matchedPrice: BigDecimal,
    val matchedQuantity: BigDecimal,
    val takerCommision: BigDecimal,
    val takerCommisionAsset: String,
    val makerCommision: BigDecimal,
    val makerCommisionAsset: String,
    val tradeDateTime: LocalDateTime,
    val executionReference: String? = null,
    val baseAsset: String? = null,
    val quoteAsset: String? = null,
    val takerSettlementActionId: String? = null,
    val makerSettlementActionId: String? = null,
    val takerFeeActionId: String? = null,
    val makerFeeActionId: String? = null
)

@Component
class EventlogRichTradeListener(private val writer: AccountingTransactionLogWriter) :
    MessageListener<String, RichTradeAccountingEvent> {

    override fun onMessage(record: ConsumerRecord<String, RichTradeAccountingEvent>) = runBlocking<Unit> {
        val trade = record.value()
        val pair = trade.pair.split('_')
        val base = trade.baseAsset ?: pair.takeIf { it.size == 2 }?.get(0)
        val quote = trade.quoteAsset ?: pair.takeIf { it.size == 2 }?.get(1)
        require(!base.isNullOrBlank() && !quote.isNullOrBlank()) { "Trade requires explicit base and quote assets" }
        require(trade.matchedQuantity.signum() > 0 && trade.matchedPrice.signum() > 0) { "Trade execution values must be positive" }
        fun side(direction: String) = when (direction.uppercase()) {
            "ASK" -> "SELL"
            "BID" -> "BUY"
            else -> error("Unsupported trade direction: $direction")
        }
        val takerSide = side(trade.takerDirection)
        val makerSide = side(trade.makerDirection)
        require(takerSide != makerSide) { "Trade participants must have opposite directions" }
        // The original numeric ID is informational and may repeat after an engine restart.
        val execution = trade.executionReference ?: "richTrade:${record.partition()}:${record.offset()}"
        val quoteAmount = trade.matchedPrice.multiply(trade.matchedQuantity)
        val seller = if (takerSide == "SELL") trade.takerUuid else trade.makerUuid
        val buyer = if (takerSide == "BUY") trade.takerUuid else trade.makerUuid
        fun participant(taker: Boolean): AccountingTransactionEvent {
            val role = if (taker) "TAKER" else "MAKER"
            val action = if (taker) trade.takerSettlementActionId else trade.makerSettlementActionId
            val otherAction = if (taker) trade.makerSettlementActionId else trade.takerSettlementActionId
            val direction = if (taker) takerSide else makerSide
            val user = if (taker) trade.takerUuid else trade.makerUuid
            return AccountingTransactionEvent(
                txId = "$execution:$role", eventId = "$execution:$role", operationId = execution,
                originalTradeId = trade.id.toString(), userId = user,
                type = AccountingTransactionType.TRADE, symbol = trade.pair,
                asset = base, amount = trade.matchedQuantity,
                baseAsset = base, quoteAsset = quote, quoteAmount = quoteAmount,
                price = trade.matchedPrice, quantity = trade.matchedQuantity,
                tradeSide = direction, participantRole = role,
                orderReference = if (taker) trade.takerOuid else trade.makerOuid,
                counterpartyOrderReference = if (taker) trade.makerOuid else trade.takerOuid,
                financialActionId = action, relatedFinancialActionId = otherAction,
                feeActionId = if (taker) trade.takerFeeActionId else trade.makerFeeActionId,
                businessReference = action?.let { "accountant:fiActions:$it" },
                // Assessed commission is not evidence that the fee transfer completed.
                fee = null,
                expectedFee = if (taker) trade.takerCommision else trade.makerCommision,
                expectedFeeAsset = if (taker) trade.takerCommisionAsset else trade.makerCommisionAsset,
                recordRole = "TRADE_EXECUTION", senderPartyId = seller, receiverPartyId = buyer,
                perspectivePartyId = user,
                // IN/OUT is relative to this participant and the row's base asset.
                side = if (direction == "SELL") "OUT" else "IN",
                sourceAsset = if (direction == "SELL") base else quote,
                sourceAmount = if (direction == "SELL") trade.matchedQuantity else quoteAmount,
                destinationAsset = if (direction == "SELL") quote else base,
                destinationAmount = if (direction == "SELL") quoteAmount else trade.matchedQuantity,
                source = "CORE_ACCOUNTANT_TRADE", referenceId = execution,
                occurredAt = trade.tradeDateTime
            )
        }
        writer.saveAll(listOf(participant(true), participant(false)))
    }
}
