package co.nilin.opex.api.core.inout

enum class TimeInForce {
    GTC,    //Good Til Canceled, An order will be on the book unless the order is canceled.
    IOC,    //Immediate Or Cancel, An order will try to fill the order as much as it can before the order expires.
    FOK,    //Fill or Kill, An order will expire if the full order cannot be filled upon execution.
}

enum class OrderStatus {

    NEW,    //The order has been accepted by the engine.
    PARTIALLY_FILLED, //A part of the order has been filled.
    FILLED,    //The order has been completed.
    CANCELED,    //The order has been canceled by the user.
    PENDING_CANCEL,    //Currently unused
    REJECTED,    //The order was not accepted by the engine and not processed.
    EXPIRED  //The order was canceled according to the order type's rules (e.g. LIMIT FOK orders with no fill, LIMIT IOC or MARKET orders that partially fill) or by the exchange, (e.g. orders canceled during liquidation, orders canceled during maintenance)
}

enum class OrderType {
    LIMIT, // 	timeInForce, quantity, price
    MARKET, // 	quantity or quoteOrderQty
    STOP_LOSS, // 	quantity, stopPrice
    STOP_LOSS_LIMIT, // 	timeInForce, quantity, price, stopPrice
    TAKE_PROFIT, // 	quantity, stopPrice
    TAKE_PROFIT_LIMIT, // 	timeInForce, quantity, price, stopPrice
    LIMIT_MAKER, // 	quantity, price
}

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderResponseType {
    ACK, RESULT, FULL
}