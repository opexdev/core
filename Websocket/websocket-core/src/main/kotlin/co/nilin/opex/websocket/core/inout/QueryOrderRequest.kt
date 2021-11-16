package co.nilin.opex.websocket.core.inout

data class QueryOrderRequest(
    val symbol: String,
    val orderId: Long?,
    val origClientOrderId: String?
)