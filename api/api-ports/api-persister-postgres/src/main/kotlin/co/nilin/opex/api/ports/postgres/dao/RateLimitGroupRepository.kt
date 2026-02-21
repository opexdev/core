package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.RateLimitGroupModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface RateLimitGroupRepository : ReactiveCrudRepository<RateLimitGroupModel, Long> {
    fun findByEnabledTrue(): Flux<RateLimitGroupModel>

}
