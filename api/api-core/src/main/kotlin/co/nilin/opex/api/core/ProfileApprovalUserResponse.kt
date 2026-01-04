package co.nilin.opex.api.core

import co.nilin.opex.api.core.inout.ProfileApprovalRequestStatus
import java.time.LocalDateTime

data class ProfileApprovalUserResponse(
    var status: ProfileApprovalRequestStatus,
    var createDate: LocalDateTime,
    var description: String? = null
)
