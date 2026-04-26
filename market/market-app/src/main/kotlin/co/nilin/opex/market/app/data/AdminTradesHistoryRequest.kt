package co.nilin.opex.market.app.data

data class AdminTradesHistoryRequest(
    val baseAsset: String?,
    val quoteAsset: String?,
    val makerUuid: String?,
    val takerUuid: String?,
    val fromDate: Long?,
    val toDate: Long?,
    val excludeSelfTrade: Boolean = true,
    val ascendingByTime: Boolean = false,
    val limit: Int,
    val offset: Int,
)
