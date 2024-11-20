package co.nilin.opex.api.ports.proxy.data

data class TransactionRequest(
    val coin: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int,
    val offset: Int,
    val ascendingByTime: Boolean? = false
)