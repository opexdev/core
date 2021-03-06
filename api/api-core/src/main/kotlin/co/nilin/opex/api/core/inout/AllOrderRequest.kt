package co.nilin.opex.api.core.inout

import java.util.*

class AllOrderRequest(
    val symbol: String?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int? = 500, //Default 500; max 1000.
)