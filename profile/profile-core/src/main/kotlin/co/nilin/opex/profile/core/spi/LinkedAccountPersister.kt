package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.linkedbankAccount.*
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface LinkedAccountPersister {
    suspend fun addNewAccount(linkedBankAccountRequest: LinkedBankAccountRequest):Mono<LinkedAccountResponse>?

    suspend fun updateAccount(updateRelatedAccountRequest: UpdateRelatedAccountRequest): Mono<LinkedAccountResponse>?


    suspend fun getAccounts(userId:String): Flow<LinkedAccountResponse>?

    suspend fun getHistory(userId:String): Flow<LinkedAccountHistoryResponse>?


    suspend fun verifyAccount(verifyRequest:VerifyLinkedAccountRequest): Mono<LinkedAccountResponse>?

    suspend fun deleteAccount(deleteLinkedAccountRequest: DeleteLinkedAccountRequest)

}