package co.nilin.opex.api.core.inout

data class UpdateApprovalRequestBody(
    val id: Long,
    val description: String?,
    val status: ProfileApprovalRequestStatus
)
