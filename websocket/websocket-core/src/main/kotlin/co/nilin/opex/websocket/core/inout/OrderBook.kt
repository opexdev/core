package co.nilin.opex.websocket.core.inout

import java.math.BigDecimal

data class OrderBook(
    val price: BigDecimal?,
    val quantity: BigDecimal?
)