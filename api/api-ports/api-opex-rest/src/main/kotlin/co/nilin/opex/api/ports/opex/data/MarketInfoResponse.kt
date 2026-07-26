package co.nilin.opex.api.ports.opex.data

data class MarketInfoResponse(
    val activeUsers: Long,
    val totalOrders: Long,
    val totalTrades: Long
)
