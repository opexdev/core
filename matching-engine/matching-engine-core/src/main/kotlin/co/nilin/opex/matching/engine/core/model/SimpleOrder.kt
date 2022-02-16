package co.nilin.opex.matching.engine.core.model

data class SimpleOrder(
    var id: Long?,
    val ouid: String,
    val uuid: String,
    val price: Long,
    val quantity: Long,
    val matchConstraint: MatchConstraint,
    val orderType: OrderType,
    val direction: OrderDirection,
    var filledQuantity: Long,
    var worse: SimpleOrder?,
    var better: SimpleOrder?,
    var bucket: Bucket?
) : Order {

    fun remainedQuantity() = quantity - filledQuantity

    override fun id(): Long? = id

    override fun toString(): String {
        return "SimpleOrder(id=$id, price=$price, quantity=$quantity, matchConstraint=$matchConstraint, orderType=$orderType, filledQuantity=$filledQuantity, worse=${worse?.id}, better=${better?.id}, bucket=${bucket?.totalQuantity})"
    }

    override fun persistent(): PersistentOrder {
        return PersistentOrder(
            id!!,
            ouid,
            uuid,
            price,
            quantity,
            matchConstraint,
            orderType,
            direction,
            filledQuantity
        )
    }
}