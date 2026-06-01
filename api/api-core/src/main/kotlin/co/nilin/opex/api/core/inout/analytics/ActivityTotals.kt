package co.nilin.opex.api.core.inout.analytics

import java.math.BigDecimal


data class ActivityTotals(
    val totalBalance: BigDecimal,
    val totalWithdraw: BigDecimal,
    val totalDeposit: BigDecimal,
    val totalTrade: BigDecimal,
    val totalOrder: BigDecimal
)
