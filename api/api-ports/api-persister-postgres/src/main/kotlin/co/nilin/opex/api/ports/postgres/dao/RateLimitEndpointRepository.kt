package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.RateLimitEndpointModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface RateLimitEndpointRepository : ReactiveCrudRepository<RateLimitEndpointModel, Long> {
    fun findByEnabledTrue(): Flux<RateLimitEndpointModel>

}