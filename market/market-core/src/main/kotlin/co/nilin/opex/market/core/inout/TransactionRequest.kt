package co.nilin.opex.market.core.inout

data class TransactionRequest(
        val coin: String?,
        val category: String?,
        val startTime: Long? = null,
        val endTime: Long? = null,
        val limit: Int? = 10,
        val offset: Int? = 0,
        val ascendingByTime: Boolean? = false,
        var owner: String? = null
)