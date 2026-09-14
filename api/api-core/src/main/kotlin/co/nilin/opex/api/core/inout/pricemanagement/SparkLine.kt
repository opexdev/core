package co.nilin.opex.api.core.inout.pricemanagement

import java.math.BigDecimal

data class SparkLineView(
    val symbol: String,
    val isTrendUp: Boolean,
    /** Percentage change over the requested period, 2 decimals. */
    val changePercent: BigDecimal,
    /** Base64-encoded SVG chart. */
    val svgData: String
)
