package co.nilin.opex.api.ports.binance.data

import java.math.BigDecimal

data class FillsData(
    val price: BigDecimal,
    val qty: BigDecimal,
    val commission: BigDecimal,
    val commissionAsset: String
)