package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class UserLimit(
    var level: String = "",
    var owner: String = "",
    var action: String = "",
    var walletType: String = "",
    var withdrawFee: BigDecimal = BigDecimal.ZERO,
    var dailyTotal: BigDecimal = BigDecimal.ZERO,
    var dailyCount: Int = 0,
    var monthlyTotal: BigDecimal = BigDecimal.ZERO,
    var monthlyCount: Int = 0
)
