package co.nilin.opex.accountant.core.inout

class TradedOrder(
    orderId: Long? = 0,
    pair: String,
    ouid: String,
    uuid: String,
    val status: Int = 0
) : OrderEvent(orderId, pair, ouid, uuid)