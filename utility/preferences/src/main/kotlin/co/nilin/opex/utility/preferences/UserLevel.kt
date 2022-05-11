package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class UserLevel(
    var level: String,
    var withdrawFee: BigDecimal,
    var dailyTotal: BigDecimal,
    var dailyCount: Int,
    var monthlyTotal: BigDecimal,
    var monthlyCount: Int
)
