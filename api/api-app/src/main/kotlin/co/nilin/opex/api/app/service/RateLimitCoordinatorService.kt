package co.nilin.opex.api.app.service

import co.nilin.opex.api.app.data.BlockResult
import org.springframework.stereotype.Component

@Component
class RateLimitCoordinatorService(
    private val rateLimiterService: RateLimiterService,
    private val penaltyService: RateLimitPenaltyService
) {


    fun check(
        identity: String,
        groupId: Long,
        maxRequests: Int,
        windowSeconds: Int,
        apiPath: String,
        apiMethod: String
    ): BlockResult {

        val blocked = penaltyService.isBlocked(identity, apiPath, apiMethod)
        if (blocked.blocked) {
            return blocked
        }

        val allowed = rateLimiterService.checkRateLimit(
            identity = identity,
            maxRequests = maxRequests,
            windowInSeconds = windowSeconds,
            apiPath = apiPath,
            apiMethod = apiMethod
        )

        return if (allowed) {
            penaltyService.onAllowed(identity, groupId, apiPath, apiMethod)
            BlockResult(blocked = false)
        } else {
            penaltyService.onLimit(identity, groupId, apiPath, apiMethod)
        }
    }
}
