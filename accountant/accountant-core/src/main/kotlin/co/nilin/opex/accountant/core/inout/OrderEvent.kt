package co.nilin.opex.accountant.core.inout

abstract class OrderEvent(
    val orderId: Long? = 0,
    val pair: String,
    val ouid: String,
    val uuid: String,
)