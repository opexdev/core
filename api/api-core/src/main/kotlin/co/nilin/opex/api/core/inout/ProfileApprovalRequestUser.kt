package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class ProfileApprovalRequestUser(
    var status: ProfileApprovalRequestStatus,
    var createDate: LocalDateTime,
    var description: String? = null
)