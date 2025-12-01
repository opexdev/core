package co.nilin.opex.api.core.inout

data class ProfileApprovalRequestFilter(
    val userId: String?,
    val status: ProfileApprovalRequestStatus?,
    val createDateFrom: Long?,
    val createDateTo: Long?,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false,

    )