package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus
import java.time.LocalDateTime

open class ProfileApprovalRequest {
    lateinit var userId: String
    var status: ProfileApprovalRequestStatus? = ProfileApprovalRequestStatus.PENDING
    var createDate: LocalDateTime? = null
    var updateDate: LocalDateTime? = null
    var updater: String? = null
    var description: String? = null
}