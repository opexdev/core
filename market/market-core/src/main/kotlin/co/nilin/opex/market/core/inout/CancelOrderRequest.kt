package co.nilin.opex.market.core.inout

class CancelOrderRequest(val ouid: String, val uuid: String, val orderId: Long, val symbol: String)