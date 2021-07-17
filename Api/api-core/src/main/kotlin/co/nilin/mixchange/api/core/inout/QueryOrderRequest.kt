package co.nilin.mixchange.api.core.inout

data class QueryOrderRequest(
        val symbol: String,
        val orderId: Long?,
        val origClientOrderId: String?
)