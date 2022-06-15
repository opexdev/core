package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class OrderBook(
    val price: BigDecimal?,
    val quantity: BigDecimal?
)