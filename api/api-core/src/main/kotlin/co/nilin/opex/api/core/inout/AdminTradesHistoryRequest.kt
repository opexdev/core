package co.nilin.opex.api.core.inout

data class AdminTradesHistoryRequest(
    val baseAsset: String?,
    val quoteAsset: String?,
    val makerUuid: String?,
    val takerUuid: String?,
    val fromDate: Long?,
    val toDate: Long?,
    val excludeSelfTrade: Boolean = true,
    val limit: Int,
    val offset: Int,
    val includeNames: Boolean = false,
)
