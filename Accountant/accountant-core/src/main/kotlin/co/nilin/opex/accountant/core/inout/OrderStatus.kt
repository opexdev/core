package co.nilin.opex.accountant.core.inout

enum class OrderStatus(val code: Int) {
    REQUESTED(0),
    NEW(1),    //The order has been accepted by the engine.
    PARTIALLY_FILLED(4), //A part of the order has been filled.
    FILLED(5),    //The order has been completed.
    CANCELED(2),    //The order has been canceled by the user.
    REJECTED(3),    //The order was not accepted by the engine and not processed.
    EXPIRED(6)  //The order was canceled according to the order type's rules (e.g. LIMIT FOK orders with no fill, LIMIT IOC or MARKET orders that partially fill) or by the exchange, (e.g. orders canceled during liquidation, orders canceled during maintenance)
}