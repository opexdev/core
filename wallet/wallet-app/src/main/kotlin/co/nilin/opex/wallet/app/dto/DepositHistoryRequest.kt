package co.nilin.opex.wallet.app.dto

data class DepositHistoryRequest(
    val currency: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false
)
