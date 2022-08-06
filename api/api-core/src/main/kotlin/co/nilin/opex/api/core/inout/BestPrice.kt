package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class BestPrice(
    val symbol: String,
    val bidPrice: BigDecimal?,
    val askPrice: BigDecimal?,
)