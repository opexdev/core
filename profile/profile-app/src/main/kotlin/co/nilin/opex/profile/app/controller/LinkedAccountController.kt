package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.LinkAccountManagement
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedAccountResponse
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedBankAccountRequest
import co.nilin.opex.profile.core.data.linkedbankAccount.UpdateRelatedAccountRequest
import co.nilin.opex.profile.core.data.linkedbankAccount.VerifyLinkedAccountRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/profile/related-account")
class LinkedAccountController(val linkedAccountManagement: LinkAccountManagement) {
    @PostMapping("/{userId}")
    suspend fun addLinkedAccount(@PathVariable userId: String,
                                 @RequestBody linkedBankAccountRequest: LinkedBankAccountRequest): LinkedAccountResponse? {
        linkedBankAccountRequest.userId = userId
        return linkedAccountManagement.addNewAccount(linkedBankAccountRequest)?.awaitFirstOrNull()
    }

    enum class Status { Enable, Disable }
    @PutMapping("/{accountId}")
    suspend fun updateLinkedAccount(@PathVariable accountId: String, @RequestBody updateRelatedAccountRequest: UpdateRelatedAccountRequest): LinkedAccountResponse? {
        return linkedAccountManagement.updateAccount(updateRelatedAccountRequest.apply {
            this.accountId = accountId
            //todo
            //userId=SecurityContext.uid
        })?.awaitFirstOrNull()
    }

    @GetMapping("/{userId}")
    //check userId and ContextSecurity
    suspend fun getLinkedAccount(@PathVariable userId: String): Flow<LinkedAccountResponse>? {
        return linkedAccountManagement.getAccounts(userId)
    }

    @PutMapping("/verify/{accountId}")
    //todo
    //just admin
    //setVerifier
    suspend fun verifyLinkedAccount(@PathVariable accountId: String
    ,@RequestBody verifyRequest:VerifyLinkedAccountRequest): LinkedAccountResponse? {
        verifyRequest.accountId=accountId
        return linkedAccountManagement.verifyAccount(verifyRequest)?.awaitFirstOrNull()

    }

    @DeleteMapping("/{accountId}")
    suspend//check userId and ContextSecurity
    fun deleteAccount(@PathVariable accountId: String) {
         linkedAccountManagement.deleteAccount(accountId)

    }
}