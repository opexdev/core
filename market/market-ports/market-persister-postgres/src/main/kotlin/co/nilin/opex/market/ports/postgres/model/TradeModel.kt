package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("trades")
class TradeModel(
    @Id var id: Long?,
    val tradeId: Long,
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val matchedPrice: BigDecimal,
    val matchedQuantity: BigDecimal,
    val takerPrice: BigDecimal,
    val makerPrice: BigDecimal,
    val takerCommission: BigDecimal?,
    val makerCommission: BigDecimal?,
    val takerCommissionAsset: String?,
    val makerCommissionAsset: String?,
    val tradeDate: LocalDateTime,
    val makerOuid: String,
    val takerOuid: String,
    val makerUuid: String,
    val takerUuid: String,
    val createDate: LocalDateTime
)