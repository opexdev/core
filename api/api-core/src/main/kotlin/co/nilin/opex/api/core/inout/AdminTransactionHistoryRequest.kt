package co.nilin.opex.api.core.inout

data class AdminTransactionHistoryRequest(
    val coin: String?,
    val category: TransferCategory?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false
)