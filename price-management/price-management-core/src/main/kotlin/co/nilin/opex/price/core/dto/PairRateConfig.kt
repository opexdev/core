package co.nilin.opex.price.core.dto

import java.math.BigDecimal

data class PairRateConfig(
    val symbol: String,
    /** Required for AUTO, unused (and must be null) for MANUAL. */
    val strategy: PriceStrategy?,
    /** Required for AUTO, unused (and must be null) for MANUAL. */
    val margin: BigDecimal?,
    val isActive: Boolean,
    val priceMode: PriceMode
)
