package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class PairInfoResponse(
    val pair: String,
    val baseAsset: String,
    val quoteAsset: String,
    val isAvailable: Boolean,
    val minOrder : BigDecimal,
    val maxOrder : BigDecimal,
    val orderTypes : String,
    val internalChart: Boolean,
    val globalChart: Boolean,
    val categories: List<PairCategory> = emptyList()
)