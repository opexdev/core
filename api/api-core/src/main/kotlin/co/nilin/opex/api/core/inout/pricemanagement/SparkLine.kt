package co.nilin.opex.api.core.inout.pricemanagement

import java.math.BigDecimal

data class SparkLineView(
    val symbol: String,
    val isTrendUp: Boolean,
    val changePercent: BigDecimal,
    val svgData: String
)
