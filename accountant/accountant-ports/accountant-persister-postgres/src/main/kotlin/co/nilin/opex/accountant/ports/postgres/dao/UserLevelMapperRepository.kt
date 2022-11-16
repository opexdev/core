package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.UserLevelMapperModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserLevelMapperRepository : ReactiveCrudRepository<UserLevelMapperModel, Long> {

    fun findByUuid(uuid: String): Mono<UserLevelMapperModel>

}