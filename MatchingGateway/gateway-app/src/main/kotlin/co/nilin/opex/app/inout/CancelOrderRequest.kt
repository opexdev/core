package co.nilin.opex.app.inout

class CancelOrderRequest(val ouid: String, var uuid: String, val orderId: Long, val symbol: String)