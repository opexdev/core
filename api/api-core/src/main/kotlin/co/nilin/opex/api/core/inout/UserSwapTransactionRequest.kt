package co.nilin.opex.api.core.inout

data class UserSwapTransactionRequest(
    val userId: String? = null,
    val sourceSymbol: String?,
    val destSymbol: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false,
    val status: ReservedStatus? = ReservedStatus.Committed
)

enum class ReservedStatus {
    Created, Expired, Committed,
}