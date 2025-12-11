package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class ProfileApprovalAdminResponse(
    var id: Long,
    var userId: String,
    var status: ProfileApprovalRequestStatus,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String? = null,
    var firstName: String? = null,
    var lastName: String? = null
)
