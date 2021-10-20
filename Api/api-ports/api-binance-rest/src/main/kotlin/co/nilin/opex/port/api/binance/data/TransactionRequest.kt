package co.nilin.opex.port.api.binance.data

data class TransactionRequest(
    val coin:String?,
    val startTime: Long,
    val endTime: Long,
    val limit: Int,
    val offset: Int
)