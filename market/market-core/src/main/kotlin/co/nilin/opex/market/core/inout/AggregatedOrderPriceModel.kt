package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class AggregatedOrderPriceModel(
    val price: BigDecimal?,
    val quantity: BigDecimal?
)