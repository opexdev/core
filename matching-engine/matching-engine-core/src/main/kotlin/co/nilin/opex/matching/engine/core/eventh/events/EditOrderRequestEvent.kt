package co.nilin.opex.matching.engine.core.eventh.events

class EditOrderRequestEvent() : CoreEvent(), OneOrderEvent {
    var ouid: String = ""
    var uuid: String = ""
    var orderId: Long = 0
    var price: Long = 0
    var quantity: Long = 0

    constructor(
        ouid: String,
        uuid: String,
        orderId: Long,
        pair: co.nilin.opex.matching.engine.core.model.Pair,
        price: Long,
        quantity: Long,
    )
            : this() {
        this.ouid = ouid
        this.uuid = uuid
        this.orderId = orderId
        this.pair = pair
        this.price = price
        this.quantity = quantity
    }

    override fun ouid(): String {
        return ouid
    }

    override fun uuid(): String {
        return uuid
    }
}