package co.nilin.opex.api.core.inout

enum class TimeInForce {
    GTC,    //Good Til Canceled, An order will be on the book unless the order is canceled.
    IOC,    //Immediate Or Cancel, An order will try to fill the order as much as it can before the order expires.
    FOK,    //Fill or Kill, An order will expire if the full order cannot be filled upon execution.
}

enum class OrderStatus(val code: Int) {

    REQUESTED(0),
    NEW(1),    //The order has been accepted by the engine.
    PARTIALLY_FILLED(4), //A part of the order has been filled.
    FILLED(5),    //The order has been completed.
    CANCELED(2),    //The order has been canceled by the user.
    REJECTED(3),    //The order was not accepted by the engine and not processed.
    EXPIRED(6);  //The order was canceled according to the order type's rules (e.g. LIMIT FOK orders with no fill, LIMIT IOC or MARKET orders that partially fill) or by the exchange, (e.g. orders canceled during liquidation, orders canceled during maintenance)

    fun isWorking(): Boolean {
        return listOf(NEW, PARTIALLY_FILLED).contains(this)
    }
}

enum class OrderType {
    LIMIT, // 	timeInForce, quantity, price
    MARKET, // 	quantity or quoteOrderQty
    STOP_LOSS, // 	quantity, stopPrice
    STOP_LOSS_LIMIT, // 	timeInForce, quantity, price, stopPrice
    TAKE_PROFIT, // 	quantity, stopPrice
    TAKE_PROFIT_LIMIT, // 	timeInForce, quantity, price, stopPrice
    LIMIT_MAKER; // 	quantity, price

    companion object {
        fun activeTypes() = listOf(LIMIT, MARKET)
    }
}

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderResponseType {
    ACK, RESULT, FULL
}