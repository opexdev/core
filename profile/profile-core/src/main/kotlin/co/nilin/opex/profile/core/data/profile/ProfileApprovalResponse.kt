package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class ProfileApprovalResponse(
    var id : Long,
    var profileId: Long,
    var status: ProfileApprovalRequestStatus,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime? = null,
    var updater: String? = null,
    var description: String?=null

)
