package co.nilin.opex.api.core.inout

data class ProfileApprovalRequestUserResponse(
    var status: ProfileApprovalRequestStatus,
    var createDate: Long,
    var description: String? = null
)