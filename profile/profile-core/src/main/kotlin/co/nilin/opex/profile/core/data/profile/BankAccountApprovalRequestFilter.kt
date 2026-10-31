package co.nilin.opex.profile.core.data.profile

data class BankAccountApprovalRequestFilter(
    val userId: String? = null,
    val status: BankAccountStatus? = null,
    val createDateFrom: Long? = null,
    val createDateTo: Long? = null,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false,
)
