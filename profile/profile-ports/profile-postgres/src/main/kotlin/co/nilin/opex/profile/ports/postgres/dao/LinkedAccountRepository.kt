package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.LinkedBankAccountModel
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface LinkedAccountRepository : ReactiveCrudRepository<LinkedBankAccountModel, Long> {

    fun findAllByUserIdAndAccountId(userId: String, accountId: String): Mono<LinkedBankAccountModel>?

    fun findAllByUserId(userId: String): Flow<LinkedBankAccountModel>?

    fun findAllByNumber(accountNumber: String): Flow<LinkedBankAccountModel>?

    @Query("select * from linked_bank_account lbc where position(lower(:accountNumber) in lower(lbc.number))>0 ")
    fun searchAllByNumber(accountNumber: String): Flow<LinkedBankAccountModel>?


    fun findByAccountId(accountId: String): Mono<LinkedBankAccountModel>?

    fun deleteByAccountIdAndUserId(accountId: String, userId: String): Mono<Void>

}