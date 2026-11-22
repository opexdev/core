package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.trade.RichTradeLogWriter
import co.nilin.opex.eventlog.core.trade.RichTradeRecord
import co.nilin.opex.eventlog.ports.kafka.listener.inout.RichTradeEvent
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

@Component
class EventlogRichTradeListener(
    private val tradeLog: RichTradeLogWriter
) :
    MessageListener<String, RichTradeEvent> {

    override fun onMessage(record: ConsumerRecord<String, RichTradeEvent>) = runBlocking<Unit> {
        val source = record.value()
        val pair = source.pair.split('_')
        val base = source.baseAsset ?: pair.takeIf { it.size == 2 }?.get(0)
        val quote = source.quoteAsset ?: pair.takeIf { it.size == 2 }?.get(1)
        require(!base.isNullOrBlank() && !quote.isNullOrBlank()) { "Trade requires explicit base and quote assets" }
        require(source.matchedQuantity.signum() > 0 && source.matchedPrice.signum() > 0) { "Trade execution values must be positive" }
        fun side(direction: String) = when (direction.uppercase()) {
            "ASK" -> "SELL"
            "BID" -> "BUY"
            else -> error("Unsupported trade direction: $direction")
        }

        val takerSide = side(source.takerDirection)
        val makerSide = side(source.makerDirection)
        require(takerSide != makerSide) { "Trade participants must have opposite directions" }
        // The original numeric ID is informational and may repeat after an engine restart.
        val execution = source.executionReference ?: "richTrade:${record.partition()}:${record.offset()}"
        require(execution.isNotBlank()) { "Execution reference cannot be blank" }
        tradeLog.save(
            RichTradeRecord(
                executionReference = execution,
                originalTradeId = source.id,
                pair = source.pair,
                takerOuid = source.takerOuid,
                takerUuid = source.takerUuid,
                takerOrderId = source.takerOrderId,
                takerDirection = source.takerDirection,
                takerPrice = source.takerPrice,
                takerQuantity = source.takerQuantity,
                takerQuoteQuantity = source.takerQuoteQuantity,
                takerRemainedQuantity = source.takerRemainedQuantity,
                takerCommision = source.takerCommision,
                takerCommisionAsset = source.takerCommisionAsset,
                makerOuid = source.makerOuid,
                makerUuid = source.makerUuid,
                makerOrderId = source.makerOrderId,
                makerDirection = source.makerDirection,
                makerPrice = source.makerPrice,
                makerQuantity = source.makerQuantity,
                makerQuoteQuantity = source.makerQuoteQuantity,
                makerRemainedQuantity = source.makerRemainedQuantity,
                makerCommision = source.makerCommision,
                makerCommisionAsset = source.makerCommisionAsset,
                matchedPrice = source.matchedPrice,
                matchedQuantity = source.matchedQuantity,
                tradeDateTime = source.tradeDateTime,
                baseAsset = base,
                quoteAsset = quote,
                takerSettlementActionId = source.takerSettlementActionId,
                makerSettlementActionId = source.makerSettlementActionId,
                takerFeeActionId = source.takerFeeActionId,
                makerFeeActionId = source.makerFeeActionId
            ), record.topic(), record.partition(), record.offset()
        )
    }
}
