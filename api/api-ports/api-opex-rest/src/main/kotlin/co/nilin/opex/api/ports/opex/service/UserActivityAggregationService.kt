package co.nilin.opex.api.ports.opex.service

import co.nilin.opex.api.core.inout.analytics.ActivityTotals
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.toTimestamp
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserActivityAggregationService(
    private val walletProxy: WalletProxy,
    private val accountantProxy: AccountantProxy
) {

    suspend fun getLast31DaysUserStats(
        token: String,
        userId: String
    ): Map<Long, ActivityTotals> {

        val balances = walletProxy.getDailyBalanceLast31Days(token, userId)
        val withdraws = accountantProxy.getDailyWithdrawLast31Days(userId)
        val deposits = accountantProxy.getDailyDepositLast31Days(userId)
        val trades = accountantProxy.getDailyTradeLast31Days(userId)

        // Collect all dates
        val allDates = (
                balances.map { it.date } +
                        withdraws.map { it.date } +
                        deposits.map { it.date } +
                        trades.map { it.date }
                ).toSet()

        // Index by date for fast lookup
        val balanceMap = balances.associateBy { it.date }
        val withdrawMap = withdraws.associateBy { it.date }
        val depositMap = deposits.associateBy { it.date }
        val tradeMap = trades.associateBy { it.date }

        // Build final map
        return allDates.associateWith { date ->
            ActivityTotals(
                totalBalance = balanceMap[date]?.totalAmount ?: BigDecimal.ZERO,
                totalWithdraw = withdrawMap[date]?.totalAmount ?: BigDecimal.ZERO,
                totalDeposit = depositMap[date]?.totalAmount ?: BigDecimal.ZERO,
                totalTrade = tradeMap[date]?.totalAmount ?: BigDecimal.ZERO,
                totalOrder = BigDecimal.ZERO
            )
        }.mapKeys { (key, value) -> key.toTimestamp() }.toSortedMap()
    }
}