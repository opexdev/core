package co.nilin.opex.eventlog.app.service

import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.core.trade.*
import co.nilin.opex.eventlog.core.swap.*
import co.nilin.opex.eventlog.core.funding.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PersistedAccountingEventFeedService(
    private val accountingTransactionLogReader: AccountingTransactionLogReader,
    private val richTradeLogReader: RichTradeLogReader,
    private val swapLogReader: SwapLogReader,
    private val fundingLogReader: FundingLogReader
) : AccountingEventFeedService {

    override suspend fun findEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        val safeLimit = limit.coerceIn(1, 10_000)
        val funding = fundingLogReader.findReportable(from, to, safeLimit).map { it.toAccountingEvent() }
        val capturedOperations = funding.map { it.type to it.operationId }.toSet()
        val legacyWithdrawals = accountingTransactionLogReader.findReportableWithdrawals(from, to, safeLimit)
            .filterNot { (it.type to it.operationId) in capturedOperations }
        val directLoggedTransactions = collectEligibleDirectTransactions(from, to, safeLimit) + legacyWithdrawals + funding
        return combineAndLimit(directLoggedTransactions, from, to, safeLimit)
    }

    override suspend fun findRawEvents(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        require(!to.isBefore(from)) { "to must be greater than or equal to from" }
        val safeLimit = limit.coerceIn(1, 10_000)
        val directLoggedTransactions = accountingTransactionLogReader.findBetween(from, to, safeLimit) +
            fundingLogReader.findBetween(from, to, safeLimit).map { it.toAccountingEvent() }
        return combineAndLimit(directLoggedTransactions, from, to, safeLimit)
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
        // Fee settlement facts stay in the log for action-ID linkage, not as extra operation rows.
        if (type == AccountingTransactionType.FEE) return false
        if (type == AccountingTransactionType.DEPOSIT) return recordRole == "DEPOSIT_LIFECYCLE" && side == "IN"
        if (type == AccountingTransactionType.SWAP) return recordRole == "SWAP_LEG"
        if (type == AccountingTransactionType.TRADE) return false
        // Withdrawals are selected separately, after collapsing lifecycle states per operation.
        return type != AccountingTransactionType.WITHDRAW
    }

    private suspend fun combineAndLimit(
        direct: List<AccountingTransactionEvent>,
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> {
        val trades = richTradeLogReader.findBetween(from, to, limit).flatMap { it.toAccountingEvents() }
        val swaps = swapLogReader.findBetween(from, to, limit).flatMap { it.toAccountingEvents() }
        return (trades + swaps + direct)
            .sortedByDescending { it.occurredAt }
            .distinctBy { (it.eventId ?: it.txId) to it.source }
            .take(limit)
    }
}
