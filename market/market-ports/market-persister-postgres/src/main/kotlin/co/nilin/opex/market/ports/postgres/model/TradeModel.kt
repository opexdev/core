package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("trades")
class TradeModel(
    @Id var id: Long?,
    @Column("trade_id") val tradeId: Long,
    val symbol: String,
    @Column("matched_quantity") val matchedQuantity: BigDecimal,
    @Column("taker_price") val takerPrice: BigDecimal,
    @Column("maker_price") val makerPrice: BigDecimal,
    @Column("taker_commision") val takerCommision: BigDecimal?,
    @Column("maker_commision") val makerCommision: BigDecimal?,
    @Column("taker_commision_asset") val takerCommisionAsset: String?,
    @Column("maker_commision_asset") val makerCommisionAsset: String?,
    @Column("trade_date") val tradeDate: LocalDateTime,
    @Column("maker_ouid") val makerOuid: String,
    @Column("taker_ouid") val takerOuid: String,
    @Column("maker_uuid") val makerUuid: String,
    @Column("taker_uuid") val takerUuid: String,
    @Column("create_date") val createDate: LocalDateTime
)