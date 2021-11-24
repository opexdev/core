package co.nilin.opex.wallet.app.dto

data class TransactionRequest(
    val coin: String?,
    val startTime: Long,
    val endTime: Long,
    val limit: Int,
    val offset: Int
)