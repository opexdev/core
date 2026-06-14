package co.nilin.opex.api.core.inout

data class AdminTradesHistoryRequest(
    val symbol: String?,
    val baseAsset: String?,
    val quoteAsset: String?,
    val ouid: String?,
    val makerOuid: String?,
    val takerOuid: String?,
    val uuid: String?,
    val makerUuid: String?,
    val takerUuid: String?,
    val fromDate: Long?,
    val toDate: Long?,
    val ascendingByTime: Boolean? = false,
    val excludeSelfTrade: Boolean? = true,
    val limit: Int?=10,
    val offset: Int?=0,
    val includeNames: Boolean? = false,
)
