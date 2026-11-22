package co.nilin.opex.eventlog.core.trade

import java.math.BigDecimal
import java.time.LocalDateTime

/** Complete financial execution source. Commissions are assessed, not proof of collection. */
data class RichTradeRecord(
    val executionReference: String,
    val originalTradeId: Long,
    val pair: String,
    val takerOuid: String,
    val takerUuid: String,
    val takerOrderId: Long,
    val takerDirection: String,
    val takerPrice: BigDecimal,
    val takerQuantity: BigDecimal,
    val takerQuoteQuantity: BigDecimal,
    val takerRemainedQuantity: BigDecimal,
    val takerCommision: BigDecimal,
    val takerCommisionAsset: String,
    val makerOuid: String,
    val makerUuid: String,
    val makerOrderId: Long,
    val makerDirection: String,
    val makerPrice: BigDecimal,
    val makerQuantity: BigDecimal,
    val makerQuoteQuantity: BigDecimal,
    val makerRemainedQuantity: BigDecimal,
    val makerCommision: BigDecimal,
    val makerCommisionAsset: String,
    val matchedPrice: BigDecimal,
    val matchedQuantity: BigDecimal,
    val tradeDateTime: LocalDateTime,
    val baseAsset: String,
    val quoteAsset: String,
    val takerSettlementActionId: String? = null,
    val makerSettlementActionId: String? = null,
    val takerFeeActionId: String? = null,
    val makerFeeActionId: String? = null
)
