package co.nilin.opex.api.ports.proxy.data

data class QueryOrderRequest(
    val symbol: String,
    val orderId: Long?,
    val origClientOrderId: String?
)