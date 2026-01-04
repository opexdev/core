package co.nilin.opex.api.app.service

import co.nilin.opex.api.app.data.BlockResult
import co.nilin.opex.api.app.data.RateLimitPenaltyState
import co.nilin.opex.api.core.spi.RateLimitConfigService
import co.nilin.opex.api.ports.postgres.util.RedisCacheHelper
import co.nilin.opex.common.utils.DynamicInterval
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.min

@Component
class RateLimitPenaltyService(private val config: RateLimitConfigService, private val redis: RedisCacheHelper) {

    fun isBlocked(identity: String, apiPath: String, apiMethod: String): BlockResult {
        val state = getPenaltyState(identity, apiPath, apiMethod) ?: return BlockResult(false)

        val now = System.currentTimeMillis()
        val bannedUntil = state.bannedUntil ?: return BlockResult(false)

        return if (bannedUntil > now) {
            BlockResult(
                blocked = true,
                retryAfterSeconds = ((bannedUntil - now) / 1000).toInt()
            )
        } else {
            BlockResult(false)
        }
    }

    fun onLimit(identity: String, groupId: Long, apiPath: String, apiMethod: String): BlockResult {
        val now = System.currentTimeMillis()
        val group = config.getGroup(groupId) ?: return BlockResult(false)
        val penalties = config.getPenalties(groupId).sortedBy { it.blockStep }

        val current = getPenaltyState(identity, apiPath, apiMethod)
        val nextViolationCount = (current?.violationCount ?: 0) + 1

        val level = min(nextViolationCount, penalties.size)
        val penalty = penalties[level - 1]

        val bannedUntil = now + Duration.ofSeconds(penalty.blockDurationSeconds.toLong()).toMillis()

        val newState = RateLimitPenaltyState(
            violationCount = nextViolationCount,
            lastViolationAt = now,
            bannedUntil = bannedUntil
        )

        val ttl = penalty.blockDurationSeconds + group.cooldownSeconds

        savePenaltyState(identity, apiPath, apiMethod, newState, ttl)

        return BlockResult(
            blocked = true,
            retryAfterSeconds = penalty.blockDurationSeconds
        )
    }

    fun onAllowed(identity: String, groupId: Long, apiPath: String, apiMethod: String) {
        val state = getPenaltyState(identity, apiPath, apiMethod) ?: return
        val group = config.getGroup(groupId) ?: return
        val now = System.currentTimeMillis()

        val lastViolation = state.lastViolationAt ?: return
        val cooldownMillis = Duration.ofSeconds(group.cooldownSeconds.toLong()).toMillis()

        if (now - lastViolation >= cooldownMillis && state.violationCount > 0) {
            val newState = state.copy(
                violationCount = state.violationCount - 1
            )
            savePenaltyState(identity, apiPath, apiMethod, newState, group.cooldownSeconds)
        }
    }

    private fun getPenaltyState(
        identity: String,
        apiPath: String,
        apiMethod: String
    ): RateLimitPenaltyState? {
        return redis.get(buildPenaltyStateKey(identity, apiPath, apiMethod))
    }

    private fun savePenaltyState(
        identity: String,
        apiPath: String,
        apiMethod: String,
        state: RateLimitPenaltyState,
        ttlSeconds: Int
    ) {
        redis.put(
            buildPenaltyStateKey(identity, apiPath, apiMethod),
            state,
            DynamicInterval(ttlSeconds, TimeUnit.SECONDS)
        )
    }

    private fun buildPenaltyStateKey(identity: String, apiPath: String, apiMethod: String): String {
        val key = "$identity:$apiMethod:$apiPath"
        return "rl:penalty:${key.hashCode()}"
    }
}