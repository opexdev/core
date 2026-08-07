package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.ManualRateLimitGroupType
import org.springframework.web.server.ServerWebExchange

interface ManualRateLimiterService {
    fun check(
        identity: String,
        group: ManualRateLimitGroupType,
        exchange: ServerWebExchange
    )
}