package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class OrderTradeData(
    val price: BigDecimal,
    val qty: BigDecimal,
    val commission: BigDecimal,
    val commissionAsset: String
)