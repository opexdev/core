package co.nilin.opex.profile.core.data.profile

data class ProfileApprovalRequestFilter(
    val userId: String?,
    val status: ProfileApprovalRequestStatus?,
    val createDateFrom: Long?,
    val createDateTo: Long?,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false,

    )