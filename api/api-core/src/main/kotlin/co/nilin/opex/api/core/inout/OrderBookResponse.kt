package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class OrderBookResponse(
    val price: BigDecimal?,
    val quantity: BigDecimal?
)