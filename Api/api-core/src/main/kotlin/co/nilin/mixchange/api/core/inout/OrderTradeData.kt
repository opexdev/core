package co.nilin.mixchange.api.core.inout

import java.math.BigDecimal

data class OrderTradeData(
        val price: BigDecimal,
        val qty: BigDecimal,
        val commission: BigDecimal,
        val commissionAsset: String
)