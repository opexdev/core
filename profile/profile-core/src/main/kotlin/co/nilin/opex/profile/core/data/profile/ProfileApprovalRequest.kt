package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class ProfileApprovalRequest(
    var userId: String,
    var status: ProfileApprovalRequestStatus? = ProfileApprovalRequestStatus.PENDING,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String?=null
)
