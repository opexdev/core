package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.model.Pair

class EditOrderRequestEvent(
    var ouid: String = "",
    var uuid: String = "",
    var orderId: Long = 0,
    pair: Pair,
    var price: Long = 0,
    var quantity: Long = 0
) : CoreEvent(pair), OneOrderEvent {

    override fun ouid(): String {
        return ouid
    }

    override fun uuid(): String {
        return uuid
    }
}