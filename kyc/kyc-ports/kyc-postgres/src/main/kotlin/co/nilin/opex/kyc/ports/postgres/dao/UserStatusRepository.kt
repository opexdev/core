package co.nilin.opex.kyc.ports.postgres.dao

import co.nilin.opex.kyc.core.data.annotation.KycLevelUpdated
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserStatusRepository : ReactiveCrudRepository<UserStatusModel, Long> {
    fun findByUserId(userId: String): Mono<UserStatusModel>?
    fun findByUserIdAndProcessId(userId: String, processId: String): Mono<UserStatusModel>?

    @Override
    @KycLevelUpdated
    fun save(data: UserStatusModel): Mono<UserStatusModel>

}