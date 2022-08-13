package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.util.*

data class MarketTrade(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val id: Long,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val time: Date,
    val isBestMatch: Boolean,
    val isMakerBuyer: Boolean
)