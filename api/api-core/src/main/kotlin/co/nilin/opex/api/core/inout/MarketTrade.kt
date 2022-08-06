package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.util.*

data class MarketTrade(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val id: Long,
    val price: BigDecimal,
    val qty: BigDecimal,
    val quoteQty: BigDecimal,
    val time: Date,
    val isBestMatch: Boolean,
    val isMakerBuyer: Boolean
)