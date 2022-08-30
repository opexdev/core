package co.nilin.opex.market.core.inout

import java.util.*

class TradeRequest(
    val symbol: String?,
    val fromTrade: Long?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int
)