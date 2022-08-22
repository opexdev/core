package co.nilin.opex.api.ports.proxy.data

import java.util.*

class AllOrderRequest(
    val symbol: String?,
    val startTime: Date?,
    val endTime: Date?,
    val limit: Int, //Default 500; max 1000.
)