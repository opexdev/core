package co.nilin.opex.api.ports.binance.data

data class RateLimitResponse(
    val rateLimitType: RateLimitType,
    val interval: String,
    val intervalNum: Int,
    val limit: Int
)