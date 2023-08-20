package co.nilin.opex.kyc.ports.postgres.dao

import co.nilin.opex.kyc.ports.postgres.model.history.UserStatusHistory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusHistoryRepository :ReactiveCrudRepository<UserStatusHistory,Long>{
    fun findAllByUserId (userId:String): Flow<UserStatusHistory>?
}