package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.history.LinkedBankAccountHistory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LinkedAccountHistoryRepository:ReactiveCrudRepository<LinkedBankAccountHistory,Long> {

    fun findAllByUserId(userId:String) :Flow<LinkedBankAccountHistory>?

    fun findAllByAccountId(accountId:String) :Flow<LinkedBankAccountHistory>?


}