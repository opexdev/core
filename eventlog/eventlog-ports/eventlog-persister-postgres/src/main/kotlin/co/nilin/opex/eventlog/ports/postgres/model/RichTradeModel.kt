package co.nilin.opex.eventlog.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("opex_rich_trades")
data class RichTradeModel(
    @Id val id: Long? = null,
    @Column("execution_reference") val executionReference: String,
    @Column("original_trade_id") val originalTradeId: Long,
    @Column("pair") val pair: String,
    @Column("taker_ouid") val takerOuid: String,
    @Column("taker_uuid") val takerUuid: String,
    @Column("taker_order_id") val takerOrderId: Long,
    @Column("taker_direction") val takerDirection: String,
    @Column("taker_price") val takerPrice: BigDecimal,
    @Column("taker_quantity") val takerQuantity: BigDecimal,
    @Column("taker_quote_quantity") val takerQuoteQuantity: BigDecimal,
    @Column("taker_remained_quantity") val takerRemainedQuantity: BigDecimal,
    @Column("taker_commision") val takerCommision: BigDecimal,
    @Column("taker_commision_asset") val takerCommisionAsset: String,
    @Column("maker_ouid") val makerOuid: String,
    @Column("maker_uuid") val makerUuid: String,
    @Column("maker_order_id") val makerOrderId: Long,
    @Column("maker_direction") val makerDirection: String,
    @Column("maker_price") val makerPrice: BigDecimal,
    @Column("maker_quantity") val makerQuantity: BigDecimal,
    @Column("maker_quote_quantity") val makerQuoteQuantity: BigDecimal,
    @Column("maker_remained_quantity") val makerRemainedQuantity: BigDecimal,
    @Column("maker_commision") val makerCommision: BigDecimal,
    @Column("maker_commision_asset") val makerCommisionAsset: String,
    @Column("matched_price") val matchedPrice: BigDecimal,
    @Column("matched_quantity") val matchedQuantity: BigDecimal,
    @Column("trade_date_time") val tradeDateTime: LocalDateTime,
    @Column("base_asset") val baseAsset: String,
    @Column("quote_asset") val quoteAsset: String,
    @Column("taker_settlement_action_id") val takerSettlementActionId: String? = null,
    @Column("maker_settlement_action_id") val makerSettlementActionId: String? = null,
    @Column("taker_fee_action_id") val takerFeeActionId: String? = null,
    @Column("maker_fee_action_id") val makerFeeActionId: String? = null,
    @Column("source_topic") val sourceTopic: String,
    @Column("source_partition") val sourcePartition: Int,
    @Column("source_offset") val sourceOffset: Long,
    @Column("captured_at") val capturedAt: LocalDateTime
)
