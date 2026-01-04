package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddBankAccountRequest
import co.nilin.opex.api.core.inout.BankAccountResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/bank-account")
class BankAccountController(
    val profileProxy: ProfileProxy,
) {

    @PostMapping
    suspend fun addBankAccount(
        @RequestBody request: AddBankAccountRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountResponse {
        return profileProxy.addBankAccount(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    suspend fun getBankAccounts(@CurrentSecurityContext securityContext: SecurityContext): List<BankAccountResponse> {
        return profileProxy.getBankAccounts(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteBankAccount(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteBankAccount(securityContext.jwtAuthentication().tokenValue(), id)
    }


}