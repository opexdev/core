package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

// Trade data for admin
data class TradeData(
    val tradeId: Long,
    val symbol: String,
    val matchedPrice: BigDecimal,
    val matchedQuantity: BigDecimal,
    val takerPrice: BigDecimal,
    val makerPrice: BigDecimal,
    val tradeDate: LocalDateTime,
    val makerUuid: String,
    val takerUuid: String,
)
