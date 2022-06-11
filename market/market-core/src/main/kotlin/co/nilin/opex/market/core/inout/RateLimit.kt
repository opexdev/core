package co.nilin.opex.market.core.inout

enum class RateLimit(val rateLimitType: RateLimitType, val interval: String, val intervalNum: Int, val limit: Int) {

    REQUEST_WEIGHT(RateLimitType.REQUEST_WEIGHT, "MINUTE", 1, 1200),
    ORDERS_SECOND(RateLimitType.ORDERS, "SECOND", 10, 50),
    ORDERS_DAY(RateLimitType.ORDERS, "DAY", 1, 16000),
    RAW_REQUESTS(RateLimitType.RAW_REQUESTS, "MINUTE", 5, 6100)

}