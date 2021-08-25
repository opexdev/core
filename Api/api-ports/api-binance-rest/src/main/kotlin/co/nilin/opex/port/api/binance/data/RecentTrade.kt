package co.nilin.opex.port.api.binance.data

data class RecentTrade(
    val id: Long,
    val price: Float,
    val qty: Float,
    val quoteQty: Float,
    val time: Long,
    val isBuyerMaker: Boolean,
    val isBestMatch: Boolean
)