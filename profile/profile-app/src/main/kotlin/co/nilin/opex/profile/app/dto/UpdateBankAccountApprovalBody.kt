package co.nilin.opex.profile.app.dto

import co.nilin.opex.profile.core.data.profile.BankAccountApprovalRequestStatus

data class UpdateBankAccountApprovalBody(
    val id: Long,
    val description: String?,
    /** APPROVED or REJECTED. */
    val status: BankAccountApprovalRequestStatus
)
