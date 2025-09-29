package co.nilin.opex.accountant.core.model

import java.math.BigDecimal

data class WithdrawLimitConfig(
    val name: String,
    val dailyMaxAmount: BigDecimal,
)
