package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

// Admin-facing order item returned by API wrapper around Market order history
// Adds creatorUuid and optional creatorOwnerName for human readability.
data class OrderAdminItem(
    val symbol: String,
    val orderId: Long,
    val orderType: MatchingOrderType,
    val side: OrderDirection,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val executedQuantity: BigDecimal,
    val takerFee: BigDecimal,
    val makerFee: BigDecimal,
    val status: Int,
    val appearance: Int,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
    // Enrichment fields (API only)
    val creatorUuid: String,
    val creatorOwnerName: String? = null,
)
