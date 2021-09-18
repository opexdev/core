package co.nilin.opex.api.core.inout

import java.util.*

class TradeRequest(
    val symbol: String?,
    val fromTrade: Long?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int? = 500 //Default 500; max 1000.
)