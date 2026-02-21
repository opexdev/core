package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.RateLimitEndpoint
import co.nilin.opex.api.core.inout.RateLimitGroup
import co.nilin.opex.api.core.inout.RateLimitPenalty
import co.nilin.opex.api.core.spi.RateLimitConfigService
import co.nilin.opex.api.ports.postgres.dao.RateLimitEndpointRepository
import co.nilin.opex.api.ports.postgres.dao.RateLimitGroupRepository
import co.nilin.opex.api.ports.postgres.dao.RateLimitPenaltyRepository
import co.nilin.opex.api.ports.postgres.model.RateLimitEndpointModel
import co.nilin.opex.api.ports.postgres.model.RateLimitGroupModel
import co.nilin.opex.api.ports.postgres.model.RateLimitPenaltyModel
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component

@Component
class RateLimitConfigImpl(
    private val groupRepo: RateLimitGroupRepository,
    private val penaltyRepo: RateLimitPenaltyRepository,
    private val endpointRepo: RateLimitEndpointRepository
) : RateLimitConfigService {

    private val groupCache = mutableMapOf<Long, RateLimitGroup>()
    private val penaltyCache = mutableMapOf<Long, List<RateLimitPenalty>>()
    private val endpointCache = mutableListOf<RateLimitEndpoint>()

    override suspend fun loadConfig() {
        val groups = groupRepo.findByEnabledTrue().collectList().awaitFirstOrElse { emptyList() }
        groupCache.clear()
        groups.forEach { groupCache[it.id!!] = it.toRateLimitGroup() }

        penaltyCache.clear()
        groups.forEach { group ->
            val penalties = penaltyRepo.findByGroupIdOrderByBlockStepAsc(group.id!!).collectList()
                .awaitFirstOrElse { emptyList() }.map { it.toRateLimitPenalty() }
            penaltyCache[group.id] = penalties
        }

        endpointCache.clear()
        endpointCache.addAll(endpointRepo.findByEnabledTrue().collectList().awaitFirstOrElse { emptyList() }
            .map { it.toRateLimitEndpoint() })
    }

    override fun getGroup(groupId: Long): RateLimitGroup? = groupCache[groupId]
    override fun getPenalties(groupId: Long): List<RateLimitPenalty> = penaltyCache[groupId] ?: emptyList()
    override fun getEndpoints(): List<RateLimitEndpoint> = endpointCache


    private fun RateLimitGroupModel.toRateLimitGroup(): RateLimitGroup =
        RateLimitGroup(id, name, requestCount, requestWindowSeconds, cooldownSeconds, maxPenaltyLevel, enabled)


    private fun RateLimitPenaltyModel.toRateLimitPenalty(): RateLimitPenalty =
        RateLimitPenalty(
            id,
            groupId,
            blockStep,
            blockDurationSeconds
        )

    private fun RateLimitEndpointModel.toRateLimitEndpoint(): RateLimitEndpoint =
        RateLimitEndpoint(
            id,
            url,
            method,
            groupId,
            priority,
            enabled
        )
}