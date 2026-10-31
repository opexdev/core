package co.nilin.opex.profile.app.dto

import co.nilin.opex.profile.core.data.profile.BankAccountStatus

data class UpdateBankAccountApprovalBody(
    val id: Long,
    val description: String?,
    val status: BankAccountStatus
)
