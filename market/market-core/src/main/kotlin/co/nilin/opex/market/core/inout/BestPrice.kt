package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class BestPrice(
    val symbol: String,
    val bidPrice: BigDecimal?,
    val askPrice: BigDecimal?,
)