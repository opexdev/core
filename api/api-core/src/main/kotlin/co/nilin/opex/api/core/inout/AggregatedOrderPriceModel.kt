package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class AggregatedOrderPriceModel(
    val price: BigDecimal?,
    val quantity: BigDecimal?
)