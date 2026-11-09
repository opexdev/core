package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.AddBankAccountAdminRequest
import co.nilin.opex.profile.app.dto.BankAccountAdminResponse
import co.nilin.opex.profile.app.dto.UpdateBankAccountApprovalBody
import co.nilin.opex.profile.app.service.BankAccountApprovalRequestManagement
import co.nilin.opex.profile.app.service.BankAccountManagement
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestFilter
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/bank-account")
class BankAccountAdminController(
    val bankAccountApprovalRequestManagement: BankAccountApprovalRequestManagement,
    val bankAccountManagement: BankAccountManagement
) {

    @GetMapping("/user/{uuid}")
    suspend fun getUserBankAccounts(@PathVariable("uuid") uuid: String): List<BankAccountAdminResponse> {
        return bankAccountManagement.getBankAccountsForAdmin(uuid)
    }

    @PostMapping("/user/{uuid}")
    suspend fun addBankAccountForUser(
        @PathVariable("uuid") uuid: String,
        @RequestBody request: AddBankAccountAdminRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountAdminResponse {
        return bankAccountManagement.addBankAccountByAdmin(uuid, request, securityContext.authentication.name)
    }

    @DeleteMapping("/user/{uuid}/{id}")
    suspend fun deleteBankAccountOfUser(
        @PathVariable("uuid") uuid: String,
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        bankAccountManagement.deleteBankAccountByAdmin(uuid, id, securityContext.authentication.name)
    }

    @PostMapping("/approval-requests")
    suspend fun getApprovalRequests(
        @RequestBody request: BankAccountApprovalRequestFilter
    ): List<BankAccountApprovalAdminResponse> {
        return bankAccountApprovalRequestManagement.getApprovalRequests(request)
    }

    @GetMapping("/approval-request/{id}")
    suspend fun getApprovalRequest(@PathVariable("id") id: Long): BankAccountApprovalAdminResponse {
        return bankAccountApprovalRequestManagement.getApprovalRequestById(id)
    }

    @PutMapping("/approval-request")
    suspend fun updateRequestStatus(
        @RequestBody changeRequestStatusBody: UpdateBankAccountApprovalBody,
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountApprovalAdminResponse {
        return bankAccountApprovalRequestManagement.changeRequestStatus(
            changeRequestStatusBody,
            securityContext.authentication.name
        )
    }
}
