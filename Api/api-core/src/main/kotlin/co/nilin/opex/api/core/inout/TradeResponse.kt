package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.util.*

data class TradeResponse(
    val symbol: String,
    val id: Long,
    val orderId: Long,
    val orderListId: Long = -1,
    val price: BigDecimal,
    val qty: BigDecimal,
    val quoteQty: BigDecimal,
    val commission: BigDecimal,
    val commissionAsset: String,
    val time: Date,
    val isBuyer: Boolean,
    val isMaker: Boolean,
    val isBestMatch: Boolean
)