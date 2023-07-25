package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.ports.postgres.model.entity.LimitationModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface LimitationRepository : ReactiveCrudRepository<LimitationModel, Long> {
    fun findByLimitationOn(data:String): Mono<LimitationModel>?
    fun deleteByLimitationOn(data:String) : Mono<Void>
    fun deleteByUserId(userId:String) : Mono<Void>

    fun deleteByActionType(actionType:ActionType) : Mono<Void>

    @Query("select * from lemitation l where (:userId is NULL or l.user_id= :userId) And (:action is NULL or l.action_type=:action); ")
    fun findAllLimitation(userId:String?,action:ActionType?) : Flow<LimitationModel>?

}