package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class Wallet(
    var currency: String,
    var mainBalance: BigDecimal,
    var dailyTotal: BigDecimal,
    var dailyCount: Int,
    var monthlyTotal: BigDecimal,
    var monthlyCount: Int
)
