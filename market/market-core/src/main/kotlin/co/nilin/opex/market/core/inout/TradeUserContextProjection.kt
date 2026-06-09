package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class TradeUserContextProjection(
    val symbol: String,
    val baseAsset: String?,
    val quoteAsset: String?,
    val id: Long,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val time: LocalDateTime,
    val isMakerBuyer: Boolean,
    val ouid: String? = null,
    val commission: BigDecimal? = null,
    val commissionAsset: String? = null,
    val isBuyer: Boolean? = null,
    val isMaker: Boolean? = null
)

