package co.nilin.opex.api.core.inout.analytics

import java.math.BigDecimal

/**
 * Totals for a single day of user activity (mock values for now).
 */
data class ActivityTotals(
    val totalBalance: BigDecimal,
    val totalWithdraw: BigDecimal,
    val totalDeposit: BigDecimal,
    val totalTrade: BigDecimal,
    val totalSwap: BigDecimal
)
