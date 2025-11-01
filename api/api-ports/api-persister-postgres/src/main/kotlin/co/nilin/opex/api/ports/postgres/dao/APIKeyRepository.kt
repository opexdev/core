package co.nilin.opex.api.ports.postgres.dao

import co.nilin.opex.api.ports.postgres.model.APIKeyModel
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface APIKeyRepository : ReactiveCrudRepository<APIKeyModel, Long> {

    fun findAllByUserId(userId: String): Flux<APIKeyModel>

    fun findByKey(key: String): Mono<APIKeyModel>?

    fun countByUserId(userId: String): Mono<Long>

}