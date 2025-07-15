package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

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