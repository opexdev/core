package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class BankAccountApprovalUserResponse(
    var bankAccountId: Long,
    var status: BankAccountApprovalRequestStatus,
    var createDate: LocalDateTime,
    var description: String? = null
)
