package co.nilin.opex.eventlog.core.accounting

import java.time.LocalDateTime

interface AccountingTransactionLogReader {
    suspend fun findBetween(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent>

    /** Latest state as of [to], with the selected transition in the requested period. */
    suspend fun findReportableWithdrawals(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int
    ): List<AccountingTransactionEvent> = selectReportableWithdrawals(findBetween(from, to, 10_000), from, to, limit)
}

/** Reference projection for bounded adapters; PostgreSQL performs this before applying LIMIT. */
fun selectReportableWithdrawals(
    events: List<AccountingTransactionEvent>,
    from: LocalDateTime,
    to: LocalDateTime,
    limit: Int
): List<AccountingTransactionEvent> {
    fun AccountingTransactionEvent.stateTime() = lifecycleTransitionAt ?: completedAt ?: occurredAt
    fun AccountingTransactionEvent.rank() = when (withdrawStatus) {
        "DONE" -> 3
        "REJECTED", "CANCELED" -> 2
        "ACCEPTED" -> 1
        else -> 0
    }
    return events.filter {
        it.type == AccountingTransactionType.WITHDRAW && it.recordRole != "INTERNAL_MOVEMENT" &&
            !it.stateTime().isAfter(to)
    }.groupBy { it.operationId ?: "${it.source}:${it.referenceId ?: it.txId}" }
        .values.map { states -> states.sortedWith(compareBy<AccountingTransactionEvent> { it.stateTime() }.thenBy { it.rank() }).last() }
        .filter {
            !it.stateTime().isBefore(from) &&
                (it.withdrawStatus in setOf("ACCEPTED", "DONE") ||
                    (it.withdrawStatus == null && it.recordRole == "WITHDRAW_COMPLETION"))
        }
        .sortedWith(compareByDescending<AccountingTransactionEvent> { it.stateTime() }.thenBy { it.eventId ?: it.txId })
        .take(limit.coerceIn(1, 10_000))
        .map { it.copy(occurredAt = it.stateTime()) }
}
