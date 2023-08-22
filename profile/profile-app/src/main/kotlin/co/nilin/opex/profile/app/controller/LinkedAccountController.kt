package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.LinkAccountManagement
import co.nilin.opex.profile.core.data.linkedbankAccount.*
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/profile/related-account")
class LinkedAccountController(val linkedAccountManagement: LinkAccountManagement) {
    @PostMapping("/{userId}")
    suspend fun addLinkedAccount(@PathVariable userId: String,
                                 @RequestBody linkedBankAccountRequest: LinkedBankAccountRequest,
                                 @CurrentSecurityContext securityContext: SecurityContext): LinkedAccountResponse? {
        if (securityContext.authentication.name != userId)
            throw OpexException(OpexError.Forbidden)
        linkedBankAccountRequest.userId = userId
        return linkedAccountManagement.addNewAccount(linkedBankAccountRequest)?.awaitFirstOrNull()
    }

    enum class Status { Enable, Disable }

    @PutMapping("/{accountId}")
    suspend fun updateLinkedAccount(@PathVariable accountId: String,
                                    @RequestBody updateRelatedAccountRequest: UpdateRelatedAccountRequest,
                                    @CurrentSecurityContext securityContext: SecurityContext): LinkedAccountResponse? {
        return linkedAccountManagement.updateAccount(updateRelatedAccountRequest.apply {
            this.accountId = accountId
            userId = securityContext.authentication.name
        })?.awaitFirstOrNull()
    }

    @GetMapping("/{userId}")
    //check userId and ContextSecurity
    suspend fun getLinkedAccount(@PathVariable userId: String,
                                 @CurrentSecurityContext securityContext: SecurityContext): Flow<LinkedAccountResponse>? {
        if (securityContext.authentication.name != userId)
            throw OpexException(OpexError.Forbidden)
        return linkedAccountManagement.getAccounts(userId)
    }

    @DeleteMapping("/{accountId}")
    suspend//check userId and ContextSecurity
    fun deleteAccount(@PathVariable accountId: String,@CurrentSecurityContext securityContext: SecurityContext) {
        linkedAccountManagement.deleteAccount(DeleteLinkedAccountRequest(accountId,securityContext.authentication.name))

    }
}