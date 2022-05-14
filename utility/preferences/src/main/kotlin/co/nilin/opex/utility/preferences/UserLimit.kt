package co.nilin.opex.utility.preferences

import java.math.BigDecimal

data class UserLimit(
    var level: String? = null,
    var owner: Long = 1,
    var action: String = "withdraw",
    var walletType: String = "main",
    var withdrawFee: BigDecimal = BigDecimal.valueOf(0.0001),
    var dailyTotal: BigDecimal = BigDecimal.valueOf(1000),
    var dailyCount: Int = 100,
    var monthlyTotal: BigDecimal = BigDecimal.valueOf(30000),
    var monthlyCount: Int = 3000
)
