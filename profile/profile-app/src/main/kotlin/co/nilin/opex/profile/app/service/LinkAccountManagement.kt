package co.nilin.opex.profile.app.service

import co.nilin.opex.profile.app.controller.LinkedAccountController
import co.nilin.opex.profile.core.data.linkedbankAccount.*
import co.nilin.opex.profile.core.spi.LinkedAccountPersister
import co.nilin.opex.profile.core.utils.isValidCardNumber
import co.nilin.opex.profile.core.utils.isValidIBAN
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LinkAccountManagement(val linkedAccountPersister: LinkedAccountPersister) {


    suspend fun addNewAccount(linkedBankAccountRequest: LinkedBankAccountRequest):Mono<LinkedAccountResponse>? {
        linkedBankAccountRequest.verifyRegisterNewAccount()
       return linkedAccountPersister.addNewAccount(linkedBankAccountRequest)

    }


    suspend fun updateAccount(updateRelatedAccountRequest: UpdateRelatedAccountRequest):Mono<LinkedAccountResponse>? {
       return linkedAccountPersister.updateAccount(updateRelatedAccountRequest)

    }
    suspend fun getAccounts(userId:String): Flow<LinkedAccountResponse>? {
        return linkedAccountPersister.getAccounts(userId)

    }

    suspend fun getHistoryLinkedAccount(accountId:String): Flow<LinkedAccountHistoryResponse>? {
        return linkedAccountPersister.getHistory(accountId)

    }


    suspend fun verifyAccount(verifyRequest: VerifyLinkedAccountRequest): Mono<LinkedAccountResponse>? {
        return linkedAccountPersister.verifyAccount(verifyRequest)

    }

    suspend fun deleteAccount(deleteLinkedAccountRequest: DeleteLinkedAccountRequest):Mono<String>? {
        return linkedAccountPersister.deleteAccount(deleteLinkedAccountRequest)

    }
    private fun LinkedBankAccountRequest.verifyRegisterNewAccount() {
        when (bankAccountType) {
            BankAccountType.Sheba -> if(!number.isValidIBAN()) throw OpexException(OpexError.InvalidIban)
            BankAccountType.Card -> if(!number.isValidCardNumber())  throw OpexException(OpexError.InvalidCard)
        }
    }


}