package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.LinkedBankAccountModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface LinkedAccountRepository:ReactiveCrudRepository<LinkedBankAccountModel,Long> {

    fun findAllByUserIdAndAccountId(userId:String,accountId:String): Mono<LinkedBankAccountModel>?

    fun findAllByUserId(userId:String): Flow<LinkedBankAccountModel>?

    fun findByAccountId(accountId: String):Mono<LinkedBankAccountModel>?

    fun deleteByAccountIdAndUserId(accountId: String,userId: String):Mono<Void>

}