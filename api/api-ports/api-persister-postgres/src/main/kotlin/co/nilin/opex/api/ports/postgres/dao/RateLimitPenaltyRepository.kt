package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.RateLimitPenaltyModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface RateLimitPenaltyRepository : ReactiveCrudRepository<RateLimitPenaltyModel, Long> {
    fun findByGroupIdOrderByBlockStepAsc(groupId: Long): Flux<RateLimitPenaltyModel>
}
