package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.UpdateBankAccountApprovalBody
import co.nilin.opex.profile.app.service.BankAccountApprovalRequestManagement
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestFilter
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
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
    val bankAccountApprovalRequestManagement: BankAccountApprovalRequestManagement
) {

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
