package co.nilin.opex.market.app.data

data class RecentTradesRequest(
    val symbol: String?,
    val makerUuid: String?,
    val takerUuid: String?,
    val fromDate: Long?,
    val toDate: Long?,
    val excludeSelfTrade : Boolean = true,
    val limit: Int,
    val offset: Int,

    )