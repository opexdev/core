package co.nilin.opex.websocket.core.inout

import java.util.*

class AllOrderRequest(
    val symbol: String?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int? = 500, //Default 500; max 1000.
)