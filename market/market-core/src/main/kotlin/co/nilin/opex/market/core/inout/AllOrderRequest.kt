package co.nilin.opex.market.core.inout

import java.util.*

class AllOrderRequest(
    val symbol: String?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int
)