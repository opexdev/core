package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class WithdrawLimitConfig(
    val name: String,
    val dailyMaxAmount: BigDecimal,
)
