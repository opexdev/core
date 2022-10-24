package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.APIKey
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface APIKeyRepository : ReactiveCrudRepository<APIKey, Long> {

    fun findAllByUserId(userId: String): Flux<APIKey>

    fun findByKey(key: String): Mono<APIKey>

    fun countByUserId(userId: String): Mono<Long>

}