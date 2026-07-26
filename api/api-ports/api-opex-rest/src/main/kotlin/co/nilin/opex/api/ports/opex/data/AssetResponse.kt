package co.nilin.opex.api.ports.opex.data

import java.math.BigDecimal

data class AssetResponse(
    val asset: String,
    var free: BigDecimal,
    var locked: BigDecimal,
    var withdrawing: BigDecimal,
)