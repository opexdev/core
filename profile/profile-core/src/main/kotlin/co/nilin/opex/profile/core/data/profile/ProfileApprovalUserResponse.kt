package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class ProfileApprovalUserResponse(
    var status: ProfileApprovalRequestStatus,
    var createDate: LocalDateTime,
    var description: String? = null
)
