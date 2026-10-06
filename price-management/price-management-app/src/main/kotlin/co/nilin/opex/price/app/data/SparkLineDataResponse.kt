package co.nilin.opex.price.app.data

import java.math.BigDecimal

data class SparkLineDataResponse(
    val symbol: String,
    val isTrendUp: Boolean,
    val changePercent: BigDecimal,
    val svgData: String
)
