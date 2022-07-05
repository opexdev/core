package co.nilin.opex.api.ports.binance.data

import java.math.BigDecimal

data class AssetResponse(
    val asset: String,
    var free: BigDecimal,
    var locked: BigDecimal,
    var withdrawing: BigDecimal,
    var ipoable: BigDecimal = BigDecimal.ZERO,
    var valuation: BigDecimal = BigDecimal.ZERO
)