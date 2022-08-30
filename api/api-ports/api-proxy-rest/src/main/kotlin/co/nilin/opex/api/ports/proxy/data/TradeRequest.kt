package co.nilin.opex.api.ports.proxy.data

import java.util.*

class TradeRequest(
    val symbol: String?,
    val fromTrade: Long?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int //Default 500; max 1000.
)