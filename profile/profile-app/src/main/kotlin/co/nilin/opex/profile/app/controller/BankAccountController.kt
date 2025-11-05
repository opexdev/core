package co.nilin.opex.profile.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.AddBankAccountRequest
import co.nilin.opex.profile.app.dto.BankAccountResponse
import co.nilin.opex.profile.app.service.BankAccountManagement
import co.nilin.opex.profile.ports.postgres.utils.RegexPatterns
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/bank-account")
class BankAccountController(
    val bankAccountManagement: BankAccountManagement,
) {


    @PostMapping
    suspend fun addBankAccount(
        @RequestBody request: AddBankAccountRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountResponse {
        validateBankAccountParams(request.cardNumber, request.iban)
        return bankAccountManagement.addBankAccount(securityContext.authentication.name, request)
    }

    @GetMapping
    suspend fun getBankAccounts(@CurrentSecurityContext securityContext: SecurityContext): List<BankAccountResponse> {
        return bankAccountManagement.getBankAccounts(securityContext.authentication.name)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteBankAccount(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        bankAccountManagement.deleteBankAccount(id, securityContext.authentication.name)
    }

    @GetMapping("/ownership")
    suspend fun isBankAccountOwnedByUser(
        @RequestParam cardNumber: String?,
        @RequestParam iban: String?,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Boolean {
        validateBankAccountParams(cardNumber, iban)
        return bankAccountManagement.isBankAccountOwnedByUser(securityContext.authentication.name, cardNumber, iban)
    }

    private fun validateBankAccountParams(cardNumber: String?, iban: String?) {
        if (
            (cardNumber.isNullOrBlank() && iban.isNullOrBlank()) ||
            (!cardNumber.isNullOrBlank() && !iban.isNullOrBlank())
        ) throw OpexError.InvalidRequestBody.exception("Either Card Number or IBAN must be provided")
        if (!iban.isNullOrBlank() && !RegexPatterns.IBAN.matches(iban)) throw OpexError.InvalidIban.exception()
    }
}