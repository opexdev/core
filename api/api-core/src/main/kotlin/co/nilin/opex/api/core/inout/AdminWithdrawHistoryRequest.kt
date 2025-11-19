package co.nilin.opex.api.core.inout

data class AdminWithdrawHistoryRequest(
    val uuid: String? = null,
    val currency: String?= null,
    val destTxRef: String?= null,
    val destAddress: String?= null,
    val status: List<WithdrawStatus> = emptyList(),
    val startTime: Long? = null,
    val endTime: Long? = null,
    val ascendingByTime: Boolean = false,
    val limit: Int? = 10,
    val offset: Int? = 0,
)
