package co.nilin.opex.api.core.inout

data class AdminSearchDepositRequest(
    val uuid: String?,
    val currency: String?,
    val sourceAddress: String?,
    val transactionRef: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val status: List<DepositStatus>? = emptyList(),
    val ascendingByTime: Boolean = false,
    val limit: Int? = 10,
    val offset: Int? = 0,
)