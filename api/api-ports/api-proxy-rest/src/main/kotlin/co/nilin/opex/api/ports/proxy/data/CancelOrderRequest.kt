package co.nilin.opex.api.ports.proxy.data

class CancelOrderRequest(val ouid: String, val uuid: String, val orderId: Long, val symbol: String)