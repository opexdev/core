package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.RateLimitEndpoint
import co.nilin.opex.api.core.inout.RateLimitGroup
import co.nilin.opex.api.core.inout.RateLimitPenalty

interface RateLimitConfigService {
    suspend fun loadConfig()
    fun getGroup(groupId: Long): RateLimitGroup?
    fun getPenalties(groupId: Long): List<RateLimitPenalty>
    fun getEndpoints(): List<RateLimitEndpoint>
}