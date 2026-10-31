package co.nilin.opex.profile.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.UpdateBankAccountApprovalBody
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalAdminResponse
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestFilter
import co.nilin.opex.profile.core.data.profile.BankAccountApprovalUserResponse
import co.nilin.opex.profile.core.data.profile.BankAccountStatus
import co.nilin.opex.profile.core.spi.BankAccountApprovalRequestPersister
import co.nilin.opex.profile.core.spi.BankAccountPersister
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BankAccountApprovalRequestManagement(
    private val bankAccountApprovalRequestPersister: BankAccountApprovalRequestPersister,
    private val bankAccountPersister: BankAccountPersister,
) {

    suspend fun getApprovalRequests(
        request: BankAccountApprovalRequestFilter
    ): List<BankAccountApprovalAdminResponse> {
        return bankAccountApprovalRequestPersister.getRequests(request)
    }

    suspend fun getApprovalRequestById(id: Long): BankAccountApprovalAdminResponse {
        return bankAccountApprovalRequestPersister.getRequestById(id)
    }

    suspend fun getApprovalRequestByBankAccountId(
        bankAccountId: Long,
        userId: String
    ): BankAccountApprovalUserResponse {
        // Reading the account first keeps one user from reading another user's review
        val bankAccount = bankAccountPersister.findById(bankAccountId)
        if (bankAccount.uuid != userId) throw OpexError.Forbidden.exception()
        return bankAccountApprovalRequestPersister.getRequestByBankAccountId(bankAccountId)
    }

    /**
     * Closes a review and moves the bank account with it. Approving verifies the account, rejecting
     * rejects it. The account's own data is never touched here.
     */
    suspend fun changeRequestStatus(
        body: UpdateBankAccountApprovalBody,
        adminId: String
    ): BankAccountApprovalAdminResponse {
        if (body.status != BankAccountStatus.VERIFIED && body.status != BankAccountStatus.REJECTED)
            throw OpexError.InvalidBankAccountApprovalRequestStatus.exception("Status must be VERIFIED or REJECTED")

        val approvalRequest = bankAccountApprovalRequestPersister.getRequestById(body.id)
        val bankAccount = bankAccountPersister.findById(approvalRequest.bankAccountId)

        if (bankAccount.status != BankAccountStatus.WAITING)
            throw OpexError.InvalidBankAccountApprovalRequestStatus.exception("This review is already closed")

        if (body.status == BankAccountStatus.VERIFIED)
            assertNotVerifiedForAnotherUser(bankAccount.uuid, bankAccount.cardNumber, bankAccount.iban)

        bankAccount.status = body.status
        bankAccount.updateDate = LocalDateTime.now()
        bankAccountPersister.update(bankAccount)

        approvalRequest.updateDate = LocalDateTime.now()
        approvalRequest.updater = adminId
        approvalRequest.description = body.description
        bankAccountApprovalRequestPersister.update(approvalRequest)

        return bankAccountApprovalRequestPersister.getRequestById(body.id)
    }

    private suspend fun assertNotVerifiedForAnotherUser(uuid: String, cardNumber: String?, iban: String?) {
        val existing = bankAccountPersister.findAll(cardNumber, iban)
        if (existing.any { it.uuid != uuid && it.status == BankAccountStatus.VERIFIED }) {
            throw when {
                !cardNumber.isNullOrBlank() -> OpexError.CardOwnershipMismatch.exception()
                else -> OpexError.IbanOwnershipMismatch.exception()
            }
        }
    }
}
