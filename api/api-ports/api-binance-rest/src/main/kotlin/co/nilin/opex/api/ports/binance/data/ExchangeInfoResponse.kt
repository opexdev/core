package co.nilin.opex.api.ports.binance.data

import co.nilin.opex.api.core.inout.FeeConfig
import java.util.*

data class ExchangeInfoResponse(
    val timezone: String = TimeZone.getDefault().id,
    val serverTime: Long = Date().time,
    val rateLimits: List<RateLimitResponse> = RateLimit.values()
        .map { RateLimitResponse(it.rateLimitType, it.interval, it.intervalNum, it.limit) },
    val exchangeFilters: List<String> = emptyList(),
    val fees: List<FeeConfig> = emptyList(),
    val symbols: List<ExchangeInfoSymbol>
)