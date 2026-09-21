package co.nilin.opex.api.core.inout.pricemanagement

import java.math.BigDecimal

enum class PriceMode { AUTO, MANUAL }

enum class PriceStrategy { MIN, MAX, AVERAGE }

data class PairRateConfigView(
    val symbol: String,
    val strategy: PriceStrategy?,
    val margin: BigDecimal?,
    val isActive: Boolean,
    val priceMode: PriceMode,
    val providers: List<String>
)

data class UpsertPairRateConfigRequest(
    val symbol: String,
    /** Required when priceMode is AUTO; must be omitted/null when MANUAL. */
    val strategy: PriceStrategy?,
    /** Required when priceMode is AUTO; must be omitted/null when MANUAL. */
    val margin: BigDecimal?,
    val isActive: Boolean = true,
    val priceMode: PriceMode,
    val providers: List<String>?,
    /** Only meaningful (and only allowed) when priceMode is MANUAL. */
    val price: BigDecimal? = null
)
