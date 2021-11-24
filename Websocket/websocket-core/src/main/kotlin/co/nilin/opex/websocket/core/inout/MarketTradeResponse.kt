package co.nilin.opex.websocket.core.inout

import java.math.BigDecimal
import java.util.*

data class MarketTradeResponse(
    val symbol: String,
    val id: Long,
    val price: BigDecimal,
    val qty: BigDecimal,
    val quoteQty: BigDecimal,
    val time: Date,
    val isBestMatch: Boolean,
    val isMakerBuyer: Boolean
)