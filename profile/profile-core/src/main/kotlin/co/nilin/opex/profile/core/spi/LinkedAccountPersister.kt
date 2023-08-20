package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedAccountResponse
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedBankAccountRequest
import co.nilin.opex.profile.core.data.linkedbankAccount.UpdateRelatedAccountRequest
import co.nilin.opex.profile.core.data.linkedbankAccount.VerifyLinkedAccountRequest
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface LinkedAccountPersister {
    suspend fun addNewAccount(linkedBankAccountRequest: LinkedBankAccountRequest):Mono<LinkedAccountResponse>?

    suspend fun updateAccount(updateRelatedAccountRequest: UpdateRelatedAccountRequest): Mono<LinkedAccountResponse>?


    suspend fun getAccounts(userId:String): Flow<LinkedAccountResponse>?

    suspend fun verifyAccount(verifyRequest:VerifyLinkedAccountRequest): Mono<LinkedAccountResponse>?

    suspend fun deleteAccount(accountId:String)

}