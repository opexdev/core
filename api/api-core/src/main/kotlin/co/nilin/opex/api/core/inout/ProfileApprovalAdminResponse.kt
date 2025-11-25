package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class ProfileApprovalAdminResponse(
    var id: Long,
    var userId: String,
    var status: ProfileApprovalRequestStatus,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String? = null

)
