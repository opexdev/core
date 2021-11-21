package co.nilin.opex.accountant.core.inout

enum class OrderStatus(val code: Int, private val orderOfAppearance: Int) {

    REQUESTED(0, 0),
    NEW(1, 1),    //The order has been accepted by the engine.
    PARTIALLY_FILLED(4, 2), //A part of the order has been filled.
    FILLED(5, 3),    //The order has been completed.
    CANCELED(2, 3),    //The order has been canceled by the user.
    REJECTED(3, 3),    //The order was not accepted by the engine and not processed.
    EXPIRED(
        6,
        3
    );  //The order was canceled according to the order type's rules (e.g. LIMIT FOK orders with no fill, LIMIT IOC or MARKET orders that partially fill) or by the exchange, (e.g. orders canceled during liquidation, orders canceled during maintenance)

    fun comesBefore(status: OrderStatus?): Boolean {
        if (status == null)
            return false
        return orderOfAppearance < status.orderOfAppearance
    }

    fun comesAfter(status: OrderStatus?): Boolean {
        if (status == null)
            return false
        return orderOfAppearance > status.orderOfAppearance
    }

    companion object {
        fun fromCode(code: Int?): OrderStatus? {
            if (code == null)
                return null
            return values().find { it.code == code }
        }
    }
}

fun Int?.comesBefore(code: Int?): Boolean {
    return OrderStatus.fromCode(this)?.comesBefore(OrderStatus.fromCode(code)) == true
}

fun Int?.comesAfter(code: Int?): Boolean {
    return OrderStatus.fromCode(this)?.comesAfter(OrderStatus.fromCode(code)) == true
}