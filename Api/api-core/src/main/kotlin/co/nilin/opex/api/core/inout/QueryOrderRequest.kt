package co.nilin.opex.api.core.inout

data class QueryOrderRequest(
    val symbol: String,
    val orderId: Long?,
    val origClientOrderId: String?
)