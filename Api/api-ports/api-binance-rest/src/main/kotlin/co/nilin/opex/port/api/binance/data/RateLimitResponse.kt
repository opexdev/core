package co.nilin.opex.port.api.binance.data

import co.nilin.opex.api.core.inout.RateLimitType

data class RateLimitResponse(
    val rateLimitType: RateLimitType,
    val interval: String,
    val intervalNum: Int,
    val limit: Int
)