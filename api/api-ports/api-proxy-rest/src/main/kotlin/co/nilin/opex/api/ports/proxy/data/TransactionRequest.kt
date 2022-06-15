package co.nilin.opex.api.ports.proxy.data

data class TransactionRequest(
    val coin: String?,
    val startTime: Long,
    val endTime: Long,
    val limit: Int,
    val offset: Int
)