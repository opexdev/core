package co.nilin.opex.websocket.core.inout

import java.math.BigDecimal

data class OrderBookResponse(
    val price: BigDecimal?,
    val quantity: BigDecimal?
)