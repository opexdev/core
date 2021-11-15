package co.nilin.opex.matching.engine.core.model

class PersistentOrder {
    var id: Long = 0
    var ouid: String = ""
    var uuid: String = ""
    var price: Long = 0
    var quantity: Long = 0
    lateinit var matchConstraint: MatchConstraint
    lateinit var orderType: OrderType
    lateinit var direction: OrderDirection
    var filledQuantity: Long = 0

    constructor() {

    }

    constructor(
        id: Long,
        ouid: String,
        uuid: String,
        price: Long,
        quantity: Long,
        matchConstraint: MatchConstraint,
        orderType: OrderType,
        direction: OrderDirection,
        filledQuantity: Long
    ) {
        this.id = id
        this.ouid = ouid
        this.uuid = uuid
        this.price = price
        this.quantity = quantity
        this.matchConstraint = matchConstraint
        this.orderType = orderType
        this.direction = direction
        this.filledQuantity = filledQuantity
    }
}