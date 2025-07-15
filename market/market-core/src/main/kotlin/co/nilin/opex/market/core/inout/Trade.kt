package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// User trade data
data class Trade(
    val symbol: String,
    val id: Long,
    val orderId: Long,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val commission: BigDecimal,
    val commissionAsset: String,
    val time: LocalDateTime,
    val isBuyer: Boolean,
    val isMaker: Boolean,
    val isBestMatch: Boolean,
    val isMakerBuyer: Boolean
)