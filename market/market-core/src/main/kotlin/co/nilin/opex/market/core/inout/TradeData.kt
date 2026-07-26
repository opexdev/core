package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

// Trade data for admin
data class TradeData(
    val tradeId: Long,
    val symbol: String,
    val baseAsset: String?,
    val quoteAsset: String?,
    val matchedPrice: BigDecimal,
    val matchedQuantity: BigDecimal,
    val takerPrice: BigDecimal,
    val makerPrice: BigDecimal,
    val tradeDate: LocalDateTime,
    val makerUuid: String,
    val takerUuid: String,
    val makerOuid: String?,
    val takerOuid: String?,
    val makerCommission: BigDecimal?,
    val takerCommission: BigDecimal?,
    val makerCommissionAsset: String?,
    val takerCommissionAsset: String?,
)
