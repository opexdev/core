package co.nilin.opex.accountant.core.inout

import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import java.math.BigDecimal

class RichOrder() {
    var orderId: Long? = 0
    lateinit var pair: String
    lateinit var ouid: String
    lateinit var uuid: String
    lateinit var userLevel: String
    lateinit var makerFee: BigDecimal
    lateinit var takerFee: BigDecimal
    lateinit var leftSideFraction: BigDecimal
    lateinit var rightSideFraction: BigDecimal
    lateinit var direction: OrderDirection
    lateinit var constraint: MatchConstraint
    lateinit var type: OrderType
    lateinit var price: BigDecimal;
    lateinit var quantity: BigDecimal;
    lateinit var executedQuantity: BigDecimal;
    lateinit var accumulativeQuoteQty: BigDecimal;
    lateinit var quoteQuantity: BigDecimal;
    var status: Int = 0;

    constructor(
        orderId: Long?,
        pair: String,
        ouid: String,
        uuid: String,
        userLevel: String,
        makerFee: BigDecimal,
        takerFee: BigDecimal,
        leftSideFraction: BigDecimal,
        rightSideFraction: BigDecimal,
        direction: OrderDirection,
        constraint: MatchConstraint,
        type: OrderType,
        price: BigDecimal,
        quantity: BigDecimal,
        quoteQuantity: BigDecimal,
        executedQuantity: BigDecimal,
        accumulativeQuoteQty: BigDecimal,
        status: Int
    ) : this() {
        this.orderId = orderId
        this.pair = pair
        this.ouid = ouid
        this.uuid = uuid
        this.userLevel = userLevel
        this.makerFee = makerFee
        this.takerFee = takerFee
        this.leftSideFraction = leftSideFraction
        this.rightSideFraction = rightSideFraction
        this.direction = direction
        this.constraint = constraint
        this.type = type
        this.price = price
        this.quantity = quantity
        this.executedQuantity = executedQuantity
        this.accumulativeQuoteQty = accumulativeQuoteQty
        this.quoteQuantity = quoteQuantity
        this.status = status
    }
}
