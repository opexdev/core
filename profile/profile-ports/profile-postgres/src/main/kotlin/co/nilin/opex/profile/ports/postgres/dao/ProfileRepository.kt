package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProfileRepository: ReactiveCrudRepository<ProfileModel, Long> {

    fun findByUserId(userId:String): Mono<ProfileModel>?
    fun findBy(pageable: Pageable): Flow<ProfileModel>
    fun findByUserIdOrEmail(userId:String,email:String): Mono<ProfileModel>?


}