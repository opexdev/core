package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.LinkAccountManagement
import co.nilin.opex.profile.core.data.linkedbankAccount.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v2/profile/linked-account")
class LinkedAccountController(val linkedAccountManagement: LinkAccountManagement) {

    @PostMapping("")
    suspend fun addLinkedAccount(
        @RequestBody linkedBankAccountRequest: LinkedBankAccountRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): LinkedAccountResponse? {
        linkedBankAccountRequest.userId = securityContext.authentication.name
        return linkedAccountManagement.addNewAccount(linkedBankAccountRequest)?.awaitFirstOrNull()
    }

    enum class Status { Enable, Disable }

    @PutMapping("/{accountId}")
    suspend fun updateLinkedAccount(
        @PathVariable accountId: String,
        @RequestBody updateRelatedAccountRequest: UpdateRelatedAccountRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): LinkedAccountResponse? {
        return linkedAccountManagement.updateAccount(updateRelatedAccountRequest.apply {
            this.accountId = accountId
            userId = securityContext.authentication.name
        })?.awaitFirstOrNull()
    }

    @GetMapping("")
    suspend fun getLinkedAccount(@CurrentSecurityContext securityContext: SecurityContext): Flow<LinkedAccountResponse>? {
        return linkedAccountManagement.getAccounts(securityContext.authentication.name)
    }

    @DeleteMapping("/{accountId}")
    suspend fun deleteAccount(
        @PathVariable accountId: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): DeleteAccountResponse? {
        return linkedAccountManagement
            .deleteAccount(DeleteLinkedAccountRequest(accountId, securityContext.authentication.name))
            ?.let { ac -> DeleteAccountResponse(ac.awaitFirst()) }
    }
}