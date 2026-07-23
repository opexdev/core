package co.nilin.opex.api.app.impl

import co.nilin.opex.api.app.service.RateLimitCoordinatorService
import co.nilin.opex.api.core.inout.ManualRateLimitGroupType
import co.nilin.opex.api.core.spi.ManualRateLimiterService
import co.nilin.opex.api.core.spi.RateLimitConfigService
import co.nilin.opex.common.OpexError
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Component
class ManualRateLimiterImpl(
    private val rateLimitConfig: RateLimitConfigService,
    private val coordinator: RateLimitCoordinatorService
) : ManualRateLimiterService {

    override fun check(
        identity: String,
        group: ManualRateLimitGroupType,
        exchange: ServerWebExchange
    ) {
        val group = rateLimitConfig.getGroup(group.name) ?: return
        val result = coordinator.check(
            identity = identity,
            groupId = group.id!!,
            maxRequests = group.requestCount,
            windowSeconds = group.requestWindowSeconds,
            apiPath = exchange.request.uri.path,
            apiMethod = exchange.request.method.name()
        )
        if (result.blocked) {
            throw OpexError.RateLimit.exception()
        }
    }
}