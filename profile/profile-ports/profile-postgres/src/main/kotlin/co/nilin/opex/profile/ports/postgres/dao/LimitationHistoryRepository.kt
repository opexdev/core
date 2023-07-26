package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.limitation.ActionType
import co.nilin.opex.profile.core.data.limitation.LimitationReason
import co.nilin.opex.profile.ports.postgres.model.entity.LimitationModel
import co.nilin.opex.profile.ports.postgres.model.history.LimitationHistory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LimitationHistoryRepository : ReactiveCrudRepository<LimitationHistory, Long> {
    @Query("select * from limitation_history l where (:userId is NULL or l.user_id= :userId) And (:action is NULL or l.action_type=:action)  And (:reason is NULL or l.reason=:reason) OFFSET :offset LIMIT :size; ")
    fun findAllLimitationHistory(userId:String?, action: ActionType?,reason:LimitationReason?, offset:Int, size:Int, pageable: Pageable) : Flow<LimitationHistory>?

}