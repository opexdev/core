package co.nilin.opex.kyc.ports.postgres.dao

import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusRepository :ReactiveCrudRepository<UserStatusModel,Long>{
    fun findByUserId(userId:String):UserStatusModel?
}