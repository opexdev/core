package co.nilin.opex.eventlog.app.source

import co.nilin.opex.eventlog.core.accounting.*
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class PersistedAccountingEventSource(
    private val tradeReader: TradeReader,
    private val accountingTransactionLogReader: AccountingTransactionLogReader
) : AccountingEventSource {

    override suspend fun collectEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        val safeLimit = limit.coerceIn(1, 10_000)
        // Raw matching events remain in opex_trades. Normalized trades arrive from richTrade.
        val directLoggedTransactions = collectEligibleDirectTransactions(from, to, safeLimit) +
            accountingTransactionLogReader.findReportableWithdrawals(from, to, safeLimit)

        return directLoggedTransactions
            .sortedByDescending { it.occurredAt }
            .distinctBy { (it.eventId ?: it.txId) to it.source }
            .take(safeLimit)
    }

    private suspend fun collectEligibleDirectTransactions(
        from: LocalDateTime,
        to: LocalDateTime,
        safeLimit: Int
    ): List<AccountingTransactionEvent> {
        val maxFetchLimit = 10_000
        var fetchLimit = safeLimit.coerceAtLeast(1)
        var previousFetchedCount = -1
        while (true) {
            val fetched = accountingTransactionLogReader.findBetween(from, to, fetchLimit)
            val eligible = fetched.filter { it.isReportingEligible() }
            if (eligible.size >= safeLimit) {
                return eligible
            }
            if (fetched.size < fetchLimit || fetchLimit >= maxFetchLimit || fetched.size == previousFetchedCount) {
                return eligible
            }
            previousFetchedCount = fetched.size
            fetchLimit = (fetchLimit * 2).coerceAtMost(maxFetchLimit)
        }
    }

    private fun AccountingTransactionEvent.isReportingEligible(): Boolean {
        if (type == AccountingTransactionType.SWAP) return recordRole == "SWAP_LEG"
        // Withdrawals are selected separately, after collapsing lifecycle states per operation.
        return type != AccountingTransactionType.WITHDRAW
    }
}
