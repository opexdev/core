package co.nilin.mixchange.port.api.binance.data

import java.math.BigDecimal

data class BalanceResponse(
    var asset: String,
    var free: BigDecimal,
    var locked: BigDecimal
)