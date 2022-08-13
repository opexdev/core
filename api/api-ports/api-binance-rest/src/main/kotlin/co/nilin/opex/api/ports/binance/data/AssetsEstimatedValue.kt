package co.nilin.opex.api.ports.binance.data

import java.math.BigDecimal

data class AssetsEstimatedValue(
    val value: BigDecimal,
    val evaluatedWith: String,
    val zeroValueAssets: List<String>
)