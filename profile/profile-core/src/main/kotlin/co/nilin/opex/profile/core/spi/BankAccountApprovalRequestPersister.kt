package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.profile.BankAccountApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequest
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestFilter
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalUserResponse

interface BankAccountApprovalRequestPersister {
    suspend fun save(request: BankAccountApprovalRequest): BankAccountApprovalRequest
    suspend fun getRequests(request: BankAccountApprovalRequestFilter): List<BankAccountApprovalAdminResponse>
    suspend fun getRequestById(id: Long): BankAccountApprovalAdminResponse
    suspend fun getRequestByBankAccountId(bankAccountId: Long): BankAccountApprovalUserResponse
    suspend fun update(request: BankAccountApprovalAdminResponse): BankAccountApprovalAdminResponse
}
