package co.nilin.opex.market.core.inout

data class QueryOrderRequest(
    val symbol: String,
    val orderId: Long?,
    val origClientOrderId: String?
)