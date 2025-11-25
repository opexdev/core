package co.nilin.opex.profile.app.dto

import co.nilin.opex.profile.core.data.profile.ProfileApprovalRequestStatus

data class UpdateApprovalRequestBody(
    val id: Long,
    val description: String?,
    val status: ProfileApprovalRequestStatus
)
